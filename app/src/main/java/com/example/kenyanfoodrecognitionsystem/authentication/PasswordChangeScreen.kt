package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.testing.toDp
import com.example.kenyanfoodrecognitionsystem.authentication.view_models.PasswordChangeViewModel

@Composable
fun PasswordChangeScreen(
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    viewModel: PasswordChangeViewModel = viewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
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

        Column (
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

            Text(
                text = "Password Change",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )

            Text(
                text = "Please enter your email",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = "Email",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(25.dp),
            )

            Spacer(Modifier.height(16.dp))

            // Display success or error messages
            uiState.successMessage?.let {
                Text(
                    text = it,
                    color = Color.Green,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    viewModel.onConfirmClick()
                    // Immediately check the state to decide whether to navigate
                    if (uiState.emailSent) {
                        onConfirmClick()
                    }
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10DAE9),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (uiState.emailSent) "Back to Settings" else "Confirm",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

            }
        }
    }

}