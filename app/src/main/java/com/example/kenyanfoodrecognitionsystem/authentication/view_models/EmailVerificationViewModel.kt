package com.example.kenyanfoodrecognitionsystem.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Define the state for the UI
data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isVerified: Boolean = false
)

// The ViewModel now accepts FirebaseAuth as a constructor parameter
class EmailVerificationViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState

    // Logic for the onResendClick action
    fun onResendClick() {
        _uiState.value = _uiState.value.copy(
            isResending = true,
            successMessage = null,
            errorMessage = null
        )
        val user = auth.currentUser
        user?.let {
            it.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = _uiState.value.copy(
                            isResending = false,
                            successMessage = "Verification email sent. Check your inbox."
                        )
                        Log.d("EmailVerification", "Verification email sent.")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isResending = false,
                            errorMessage = "Failed to send verification email. Please try again."
                        )
                        Log.e("EmailVerification", "Failed to send email.", task.exception)
                    }
                }
        } ?: run {
            _uiState.value = _uiState.value.copy(
                isResending = false,
                errorMessage = "No authenticated user found. Please try logging in again."
            )
        }
    }

    // Logic for the onContinueClick action
    fun onContinueClick() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            successMessage = null,
            errorMessage = null
        )
        val user = auth.currentUser
        user?.let {
            // It's crucial to reload the user's profile to get the latest verification status
            it.reload()
                .addOnCompleteListener { task ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (task.isSuccessful) {
                        if (it.isEmailVerified) {
                            _uiState.value = _uiState.value.copy(
                                isVerified = true,
                                successMessage = "Email successfully verified!"
                            )
                            Log.d("EmailVerification", "Email is verified.")
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Email is not yet verified. Please click the link in your email."
                            )
                            Log.d("EmailVerification", "Email not verified.")
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to refresh user status. Please check your network and try again."
                        )
                        Log.e("EmailVerification", "Failed to reload user.", task.exception)
                    }
                }
        } ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No authenticated user found."
            )
        }
    }
}
