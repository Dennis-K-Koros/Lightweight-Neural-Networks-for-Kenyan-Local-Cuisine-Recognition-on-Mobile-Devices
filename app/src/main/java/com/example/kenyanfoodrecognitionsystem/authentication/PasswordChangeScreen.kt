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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.xr.compose.testing.toDp

@Composable
fun PasswordChangeScreen(
    onBackClick: () -> Unit,
    onPasswordChangeClick: () -> Unit
){

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confPass by remember { mutableStateOf("") }
    var confPassVisible by remember { mutableStateOf(false) }

    BoxWithConstraints (
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

            Text(
                text = "Password Reset",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )

            Text(
                text = "Please enter your new password and confirm it",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(25.dp),

                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = confPass,
                onValueChange = { confPass = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (confPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password Icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(25.dp),

                trailingIcon = {
                    val image = if (confPassVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    val description = if (confPassVisible) "Hide password" else "Show password"

                    IconButton(onClick = { confPassVisible = !confPassVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onPasswordChangeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10DAE9),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp)
            ) {

                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleMedium
                )

            }

        }

    }

}

@Preview
@Composable
fun PasswordChangePreview(){
    PasswordChangeScreen(
        onBackClick = {},
        onPasswordChangeClick = {}
    )
}