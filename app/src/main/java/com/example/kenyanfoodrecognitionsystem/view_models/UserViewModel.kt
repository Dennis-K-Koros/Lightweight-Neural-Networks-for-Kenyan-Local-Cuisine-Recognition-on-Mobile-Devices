package com.example.kenyanfoodrecognitionsystem.view_models

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Custom error codes used to communicate required UI actions back to the Composable.
 */
object ReAuthCodes {
    // If the user is authenticated via Email/Password
    const val PASSWORD_REAUTH_REQUIRED = "PASSWORD_REAUTH_REQUIRED"
    // If the user is authenticated via Google
    const val GOOGLE_REAUTH_REQUIRED = "GOOGLE_REAUTH_REQUIRED"
}

/**
 * Data class representing  the non-Auth specific user data stored in firestore.
 */
data class UserProfile(
    val phone: String? = null,
    val createdAt: Long = System.currentTimeMillis()
){
    // Required no-argument constructor for Firestore to deserialize
    @Suppress("unused")
    constructor() : this(phone = null)
}


/**
 * Data class representing the complete user information, merged from Auth and Firestore.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Long = 0L
)

const val USERS_COLLECTION = "users"

/**
 * UI State for user-related operations.
 */
sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: User) : UserUiState()
    data class Error(val message: String) : UserUiState()
    object SignedOut : UserUiState() // Added state for clear sign-out indication
}

// New state for communicating the result of the update attempt
sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Success : UpdateStatus()
    data class ReAuthRequired(val providerId: String) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

// New state for communicating the result of the delete attempt
sealed class DeleteAccountStatus {
    object Idle : DeleteAccountStatus()
    object Success : DeleteAccountStatus()
    data class ReAuthRequired(val providerId: String) : DeleteAccountStatus()
    data class Error(val message: String) : DeleteAccountStatus()
}

/**
 * ViewModel responsible for managing user data and authentication state.
 * It uses a Flow to listen for real-time changes in Firebase Authentication state.
 */
class UserViewModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val db: FirebaseFirestore = Firebase.firestore,
    private val storage: FirebaseStorage = Firebase.storage
) : ViewModel() {

    // Private mutable state for the UI
    private val _userState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val userState: StateFlow<UserUiState> = _userState.asStateFlow()

    // Status to communicate the result of the profile update back to the UI
    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    // Status to communicate the result of the profile update back to the UI
    private val _deleteAccountStatus = MutableStateFlow<DeleteAccountStatus>(DeleteAccountStatus.Idle)
    val deleteAccountStatus: StateFlow<DeleteAccountStatus> = _deleteAccountStatus.asStateFlow()


    // Convenience property to hold the current user object
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Internal Flows for Auth and Firestore data
    private val authUserFlow = auth.authFlow()
    private val firestoreProfileFlow = MutableStateFlow<UserProfile?>(null)

    // --- Initialization and Real-Time Auth Listener ---

    init {
        // 1. Listen to Auth changes to know which user's Firestore profile to load
        authUserFlow.onEach { firebaseUser ->
            if (firebaseUser != null) {
                // 2. Set up real-time listener for the Firestore document
                db.collection(USERS_COLLECTION).document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            // Handle error, but don't crash the app
                            firestoreProfileFlow.value = null
                            _userState.value = UserUiState.Error("Failed to listen to profile: ${e.message}")
                            return@addSnapshotListener
                        }
                        // Convert document to our UserProfile model
                        val profile = snapshot?.toObject(UserProfile::class.java)
                        firestoreProfileFlow.value = profile
                    }
            } else {
                // Clear Firestore profile if user signs out
                firestoreProfileFlow.value = null
            }
        }.launchIn(viewModelScope)

        // 3. Combine Auth and Firestore data into the public _currentUser StateFlow
        authUserFlow.combine(firestoreProfileFlow) { firebaseUser, userProfile ->
            if (firebaseUser != null) {
                // Merge data into the complete User object
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "User",
                    email = firebaseUser.email ?: "",
                    profileImageUrl = firebaseUser.photoUrl?.toString(),
                    phone = userProfile?.phone, // Data from Firestore
                    createdAt = userProfile?.createdAt ?: 0L
                )
            } else {
                null // User is signed out
            }
        }.onEach { mergedUser ->
            _currentUser.value = mergedUser
            _userState.value = if (mergedUser != null) {
                UserUiState.Success(mergedUser)
            } else {
                UserUiState.SignedOut
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Converts Firebase's AuthStateListener into a Kotlin Flow for reactive updates.
     */
    private fun FirebaseAuth.authFlow() = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        addAuthStateListener(authStateListener)

        // The suspending block executes when the flow is closed or canceled
        awaitClose { removeAuthStateListener(authStateListener) }
    }


    /**
     * Resets the update status to Idle (e.g., after the UI has handled an error or success).
     */
    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    /**
     * Determines the user's current primary authentication provider.
     */
    private fun getPrimaryProviderId(): String? {
        // Find the provider that is not 'firebase' (which is the default anonymous/custom token)
        return auth.currentUser?.providerData?.firstOrNull {
            it.providerId != "firebase"
        }?.providerId
    }

    // --- Core Functionality ---

    /**
     * Updates the user's profile details across Firebase Auth and Firestore.
     * @return Result.Success if successful, Result.Failure with a message otherwise.
     */
    /**
     * Updates the user's profile details across Firebase Auth and Firestore.
     * This function now only handles the *attempt* and dictates if re-auth is needed.
     */
    fun updateProfile(
        newName: String,
        newPhone: String,
        newEmail: String,
        newPhotoUri: Uri?
    ) = viewModelScope.launch {
        _updateStatus.value = UpdateStatus.Idle
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _updateStatus.value = UpdateStatus.Error("User not logged in.")
                return@launch
            }

            // --- CRITICAL: Handle Email Change Security Check ---
            if (newEmail != firebaseUser.email) {
                val providerId = getPrimaryProviderId()

                val reAuthCode = when (providerId) {
                    EmailAuthProvider.PROVIDER_ID -> ReAuthCodes.PASSWORD_REAUTH_REQUIRED
                    GoogleAuthProvider.PROVIDER_ID -> ReAuthCodes.GOOGLE_REAUTH_REQUIRED
                    else -> "UNSUPPORTED_PROVIDER"
                }

                // Expose the required action and provider back to the UI
                _updateStatus.value = UpdateStatus.ReAuthRequired(reAuthCode)
                return@launch // Stop here, wait for re-authentication flow to complete
            }

            // --- If we reach here, no email change is required (or re-auth succeeded and this is the retry) ---

            // --- 1. Handle Profile Picture Update ---
            if (newPhotoUri != null && newPhotoUri.toString() != firebaseUser.photoUrl?.toString()) {
                val photoUrl = uploadImageToStorage(firebaseUser.uid, newPhotoUri).getOrThrow()

                val authUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .setPhotoUri(photoUrl)
                    .build()
                firebaseUser.updateProfile(authUpdates).await()
            }

            // --- 2. Handle Name Update ---
            if (newName != firebaseUser.displayName) {
                val authUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                firebaseUser.updateProfile(authUpdates).await()
            }

            // --- 3. Handle Phone Update (Firestore) ---
            val profileRef = db.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val currentProfile = firestoreProfileFlow.value
            if (newPhone != currentProfile?.phone) {
                val userProfileData = UserProfile(
                    phone = newPhone.ifBlank { null }
                )
                profileRef.set(userProfileData, SetOptions.merge()).await()
            }

            // Force reload Auth user to ensure latest name/photo is pulled
            firebaseUser.reload().await()

            _updateStatus.value = UpdateStatus.Success

        } catch (e: Exception) {
            _updateStatus.value = UpdateStatus.Error("Update failed: ${e.localizedMessage}")
        }
    }

    /**
     * Placeholder function to apply the email change after successful re-authentication.
     * In a real app, the UI would call this function immediately after re-authenticating.
     */
    fun applyEmailChange(newEmail: String) = viewModelScope.launch {
        try {
            auth.currentUser?.updateEmail(newEmail)?.await()
            _updateStatus.value = UpdateStatus.Success
        } catch (e: Exception) {
            _updateStatus.value = UpdateStatus.Error("Email change failed after re-auth: ${e.localizedMessage}")
        }
    }

    /**
     * Uploads the local URI image to Firebase Storage and returns the download URL.
     * This is a critical step for profile pictures.
     */
    private suspend fun uploadImageToStorage(uid: String, uri: Uri): Result<Uri> = try {
        val storageRef = storage.reference.child("profile_images/$uid/profile.jpg")
        val uploadTask = storageRef.putFile(uri).await()
        val downloadUrl = storageRef.downloadUrl.await()
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Resets the delete account status to Idle.
     */
    fun resetDeleteAccountStatus() {
        _deleteAccountStatus.value = DeleteAccountStatus.Idle
    }

    /**
     * Deletes the user's account completely, including:
     * - Profile image from Storage
     * - User document from Firestore
     * - Firebase Authentication account
     *
     * Requires recent authentication for security.
     */
    fun deleteAccount() = viewModelScope.launch {
        _deleteAccountStatus.value = DeleteAccountStatus.Idle
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _deleteAccountStatus.value = DeleteAccountStatus.Error("User not logged in.")
                return@launch
            }

            val userId = firebaseUser.uid

            // Step 1: Delete profile image from Storage (if exists)
            try {
                val storageRef = storage.reference.child("profile_images/$userId/profile.jpg")
                storageRef.delete().await()
            } catch (e: Exception) {
                // Ignore if image doesn't exist, but log for debugging
                println("Profile image deletion skipped or failed: ${e.message}")
            }

            // Step 2: Delete user document from Firestore
            try {
                db.collection(USERS_COLLECTION).document(userId).delete().await()
            } catch (e: Exception) {
                _deleteAccountStatus.value = DeleteAccountStatus.Error("Failed to delete user data: ${e.localizedMessage}")
                return@launch
            }

            // Step 3: Delete Firebase Authentication account
            try {
                firebaseUser.delete().await()
                _deleteAccountStatus.value = DeleteAccountStatus.Success
            } catch (e: Exception) {
                // Check if re-authentication is required
                if (e.message?.contains("requires-recent-login", ignoreCase = true) == true) {
                    val providerId = getPrimaryProviderId()
                    val reAuthCode = when (providerId) {
                        EmailAuthProvider.PROVIDER_ID -> ReAuthCodes.PASSWORD_REAUTH_REQUIRED
                        GoogleAuthProvider.PROVIDER_ID -> ReAuthCodes.GOOGLE_REAUTH_REQUIRED
                        else -> "UNSUPPORTED_PROVIDER"
                    }
                    _deleteAccountStatus.value = DeleteAccountStatus.ReAuthRequired(reAuthCode)
                } else {
                    _deleteAccountStatus.value = DeleteAccountStatus.Error("Account deletion failed: ${e.localizedMessage}")
                }
            }

        } catch (e: Exception) {
            _deleteAccountStatus.value = DeleteAccountStatus.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    /**
     * Re-authenticates the user with their password.
     * Call this before retrying deleteAccount() if PASSWORD_REAUTH_REQUIRED.
     */
    suspend fun reAuthenticateWithPassword(password: String): Result<Unit> = try {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null || firebaseUser.email == null) {
            Result.failure(Exception("User not logged in or no email available"))
        } else {
            val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, password)
            firebaseUser.reauthenticate(credential).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Re-authenticates the user with Google Sign-In.
     * The actual Google Sign-In flow should be handled by the UI/Activity.
     * Pass the resulting idToken here.
     */
    suspend fun reAuthenticateWithGoogle(idToken: String): Result<Unit> = try {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Result.failure(Exception("User not logged in"))
        } else {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseUser.reauthenticate(credential).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }


    /**
     * Sign out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
            } catch (e: Exception) {
                _userState.value = UserUiState.Error("Failed to sign out: ${e.message}")
            }
        }
    }
}



