package com.example.kenyanfoodrecognitionsystem.view_models

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// UI State for the Forgot Password screen
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val emailSent: Boolean = false
)

// The only change is adding a default parameter to the constructor for testability.
open class ForgotPasswordViewModel(private val auth: FirebaseAuth = Firebase.auth) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    open val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    open fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    // Logic for sending the password reset email
    open fun onConfirmClick() {
        val email = _uiState.value.email.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Email cannot be empty.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        // Call the Firebase function to send the password reset email
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "If an account with that email exists, a password reset link has been sent to your inbox.",
                            emailSent = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = task.exception?.message ?: "An unknown error occurred. Please try again.",
                            emailSent = false
                        )
                    }
                }
            }
    }

    // Function to clear messages when the user interacts with the UI
    open fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
