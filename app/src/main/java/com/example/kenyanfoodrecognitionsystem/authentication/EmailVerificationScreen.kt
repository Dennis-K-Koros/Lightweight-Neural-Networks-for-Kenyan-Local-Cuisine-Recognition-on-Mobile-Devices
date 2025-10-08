package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.testing.toDp
import com.example.kenyanfoodrecognitionsystem.authentication.view_models.EmailVerificationViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EmailVerificationScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    // This is the key change: create a ViewModelFactory to provide the FirebaseAuth instance.
    val viewModel: EmailVerificationViewModel = viewModel(
        factory = EmailVerificationViewModelFactory(FirebaseAuth.getInstance())
    )
    val uiState by viewModel.uiState.collectAsState()


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        val screenWidthPx: Int = constraints.maxWidth
        val screenHeightPx: Int = constraints.maxHeight

        // Use the toDp() extension function to convert the pixel values to Dp
        val screenWidth: Dp = screenWidthPx.toDp()
        val screenHeight: Dp = screenHeightPx.toDp()

        // Top-left large circle
        Canvas(
            modifier = Modifier
                .size(screenWidth * 0.9f) // Size is 80% of screen width
                .offset(x = screenWidth * -0.18f, y = screenHeight * -0.07f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GradientColorStart, GradientColorEnd)
                )
            )
        }

        // Bottom-left large circle
        Canvas(
            modifier = Modifier
                .size(screenWidth * 0.9f) // Size is 80% of screen width
                .offset(x = screenWidth * 0.4f, y = screenHeight * 0.7f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GradientColorEnd, GradientColorStart)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),

            ) {
            // Back Arrow
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            Spacer(Modifier.height(160.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Email Verification",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "A verification link has been sent to your email. Please check your inbox and click the link to verify your account.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Display messages
                uiState.successMessage?.let {
                    Text(
                        text = it,
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Button to trigger "Continue" once email is verified
            Button(
                onClick = {
                    viewModel.onContinueClick()
                    if (uiState.isVerified) {
                        onContinueClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10DAE9),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (uiState.isVerified) "Back to SignIn" else "Confirm",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Row for resending the email
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Haven't received the email?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.width(10.dp))
                if (uiState.isResending) {
                    CircularProgressIndicator(
                        color = Color(0xFF10DAE9),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Resend Link",
                        color = Color(0xFF10DAE9),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { viewModel.onResendClick() }
                    )
                }
            }
        }
    }

}



// A factory class to create an instance of EmailVerificationViewModel
// with the required FirebaseAuth dependency.
class EmailVerificationViewModelFactory(private val auth: FirebaseAuth) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailVerificationViewModel(auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
