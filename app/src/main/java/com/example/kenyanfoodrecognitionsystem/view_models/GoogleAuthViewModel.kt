package com.example.kenyanfoodrecognitionsystem.view_models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kenyanfoodrecognitionsystem.R // Import the generated resources
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// State for the UI
data class GoogleAuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val isNewUser: Boolean = false
)

// This ViewModel handles the logic for signing in with a Google ID token
class GoogleAuthViewModel(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    val _uiState = MutableStateFlow(GoogleAuthUiState())
    val uiState: StateFlow<GoogleAuthUiState> = _uiState

    // This property holds the Intent used to launch the Google sign-in activity
    val signInIntent = googleSignInClient.signInIntent

    /**
     * Attempts to sign in to Firebase using the ID Token obtained from Google.
     * @param accountIdToken The ID Token obtained from the successful Google Sign-In Activity result.
     */
    fun signInWithGoogleToken(accountIdToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)

        viewModelScope.launch {
            try {
                // 1. Create a Firebase Credential using the Google ID Token
                val credential = GoogleAuthProvider.getCredential(accountIdToken, null)

                // 2. Sign in to Firebase with the credential
                val result = auth.signInWithCredential(credential).await()

                val isNewUser = result.additionalUserInfo?.isNewUser ?: false

                if (result.user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null,
                        userDisplayName = result.user?.displayName,
                        userEmail = result.user?.email,
                        isNewUser = isNewUser
                    )
                } else {
                    throw Exception("Firebase sign-in failed unexpectedly.")
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = e.localizedMessage ?: "Google Sign-in failed."
                )
            }
        }
    }

    // Function to reset the UI state after navigation or error handling
    fun resetState() {
        _uiState.value = GoogleAuthUiState()
    }
}

// Factory to provide the ViewModel with dependencies
class GoogleAuthViewModelFactory(private val context: Context, private val auth: FirebaseAuth) :
    ViewModelProvider.Factory {

    private val webClientId: String = context.getString(R.string.default_web_client_id)

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .requestProfile() // Request user name/profile info
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoogleAuthViewModel::class.java)) {
            return GoogleAuthViewModel(auth, googleSignInClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
