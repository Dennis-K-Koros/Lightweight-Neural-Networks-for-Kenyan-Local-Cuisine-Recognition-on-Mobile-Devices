package com.example.kenyanfoodrecognitionsystem.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Represents the state of the email verification UI
data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isVerified: Boolean = false
)

class EmailVerificationViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    init {
        // This is a crucial check to handle the race condition.
        // It immediately sends the email if a user is present on ViewModel creation.
        auth.currentUser?.let {
            // Only send if the email isn't already verified
            if (!it.isEmailVerified) {
                sendVerificationEmail()
            }
        }
    }

    private fun sendVerificationEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                successMessage = null,
                errorMessage = null
            )
            val user = auth.currentUser
            if (user != null) {
                try {
                    user.sendEmailVerification().await()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "A verification link has been sent to your email."
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to send verification email: ${e.message}"
                    )
                    Log.e("EmailVerification", "Failed to send email", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No authenticated user found. Please try again."
                )
            }
        }
    }

    // Logic for the onResendClick action
    fun onResendClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResending = true, successMessage = null, errorMessage = null)
            val user = auth.currentUser
            if (user != null) {
                try {
                    user.sendEmailVerification().await()
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        successMessage = "Verification link has been re-sent successfully."
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        errorMessage = "Failed to re-send verification email. Please try again."
                    )
                    Log.e("EmailVerification", "Failed to re-send email", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isResending = false,
                    errorMessage = "No authenticated user found. Please try logging in again."
                )
            }
        }
    }

    // Logic for the onContinueClick action
    fun onContinueClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                successMessage = null,
                errorMessage = null
            )
            val user = auth.currentUser
            if (user != null) {
                try {
                    // It's crucial to reload the user's profile to get the latest verification status
                    user.reload().await()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (user.isEmailVerified) {
                        _uiState.value = _uiState.value.copy(
                            isVerified = true,
                            successMessage = "Email successfully verified!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Email is not yet verified. Please click the link in your email."
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to refresh user status. Please check your network and try again."
                    )
                    Log.e("EmailVerification", "Failed to reload user.", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No authenticated user found."
                )
            }
        }
    }
}
