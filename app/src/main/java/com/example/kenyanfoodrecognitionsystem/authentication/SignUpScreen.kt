package com.example.kenyanfoodrecognitionsystem.authentication

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.testing.toDp
import com.example.kenyanfoodrecognitionsystem.R
import com.example.kenyanfoodrecognitionsystem.view_models.GoogleAuthViewModel
import com.example.kenyanfoodrecognitionsystem.view_models.GoogleAuthViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth


@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: (name: String, phone: String, email: String, password: String) -> Unit,
    onPasswordMismatch: () -> Unit,
    onGoogleSignInSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState
){

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val googleAuthViewModel: GoogleAuthViewModel = viewModel(
        factory = GoogleAuthViewModelFactory(context, auth)
    )
    val googleUiState by googleAuthViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confPass by remember { mutableStateOf("") }
    var confPassVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Launcher for the Google Sign-in Intent
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken != null) {
                    googleAuthViewModel.signInWithGoogleToken(idToken)
                } else {
                    Log.e("GoogleAuth", "Google ID Token was null.")
                }
            } catch (e: ApiException) {
                Log.w("GoogleAuth", "Google sign in failed", e)
            }
        }
    }

    // Effect to handle navigation on Google sign-in success or display errors
    LaunchedEffect(googleUiState.isSuccess, googleUiState.error) {
        if (googleUiState.isSuccess) {
            // Check for new user only if necessary (for data saving), but navigate regardless
            onGoogleSignInSuccess()
            googleAuthViewModel.resetState()
        }
        googleUiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = "Google Sign-in failed: $error"
            )
            googleAuthViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

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

            Scaffold (

                snackbarHost = {
                    // SnackbarHost is now managed by Scaffold
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                },containerColor = Color.Transparent,
                content = { innerPadding ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .padding(innerPadding) // This is crucial for handling insets
                            .padding(horizontal = 30.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Back Arrow
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }

                        Spacer(Modifier.height(155.dp))

                        Text(
                            text = "Create An Account",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Sign In",
                                color = Color(0xFF10DAE9),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.clickable { onSignInClick() }
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Person Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(25.dp),
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(25.dp),
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = "Email Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(25.dp),
                        )

                        Spacer(Modifier.height(10.dp))

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
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(25.dp),

                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff
                                val description =
                                    if (passwordVisible) "Hide password" else "Show password"

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
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(25.dp),

                            trailingIcon = {
                                val image = if (confPassVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff
                                val description =
                                    if (confPassVisible) "Hide password" else "Show password"

                                IconButton(onClick = { confPassVisible = !confPassVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            }
                        )

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (password == confPass) {
                                    onSignUpClick(name, phone, email, password)
                                } else {
                                    // Passwords do not match, show a message
                                    onPasswordMismatch()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10DAE9),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonPin,
                                    contentDescription = "Sign Up Icon",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Sign Up",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Divider Line
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.Gray.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )

                            // "Or" Text in the center
                            Text(
                                text = "OR",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            // Right Divider Line
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.Gray.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                // Launch the Google Sign-in Activity
                                googleSignInLauncher.launch(googleAuthViewModel.signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(25.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.LightGray) // Slightly gray border
                            )

                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo ), // Placeholder: Replace with Google Icon resource
                                    contentDescription = "Google Icon",
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Sign Up with Google",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Show loading indicator
                        if (googleUiState.isLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = GradientColorStart)
                            }
                        }

                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun SignUpPreview(){
    /*SignUpScreen(
        onBackClick = {},
        onSignUpClick = { name: String, phone: String, email: String, password: String -> {} },
        onSignInClick = {},
        onPasswordMismatch = {},
        onGoogleSignUpClick = {},
        snackbarHostState = SnackbarHostState()
    )*/
}