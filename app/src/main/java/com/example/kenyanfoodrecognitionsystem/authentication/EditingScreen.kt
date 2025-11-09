package com.example.kenyanfoodrecognitionsystem.authentication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.testing.toDp
import coil.compose.AsyncImage
import com.example.kenyanfoodrecognitionsystem.view_models.ReAuthCodes
import com.example.kenyanfoodrecognitionsystem.view_models.UpdateStatus
import com.example.kenyanfoodrecognitionsystem.view_models.UserViewModel
import kotlinx.coroutines.launch


// Custom colors based on the image provided
val CyanPrimary = Color(0xFF10DAE9)

@Composable
fun EditingScreen(
    onBackClick: () -> Unit,
    onUpdateSuccess: () -> Unit,
    onGoogleReAuthRequired: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    snackbarHostState: SnackbarHostState
){

    val user by userViewModel.currentUser.collectAsState()
    val updateStatus by userViewModel.updateStatus.collectAsState()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // --- Data Initialization ---
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            phone = it.phone ?: "" // Defaults to empty string if not found in Firestore
            email = it.email

            // Prioritize local selection (Uri) over network URL (String)
            if (profilePictureUri == null && !it.profileImageUrl.isNullOrEmpty()) {
                profilePictureUri = Uri.parse(it.profileImageUrl)
            } else if (profilePictureUri != null && it.profileImageUrl.isNullOrEmpty()) {
                // If Auth clears the photo URL, clear the state too
                profilePictureUri = null
            }
        }
    }

    // --- Update Status Side Effect Handler ---
    LaunchedEffect(updateStatus) {
        when (val status = updateStatus) {
            is UpdateStatus.Idle -> {
                isUpdating = false
            }
            is UpdateStatus.Success -> {
                isUpdating = false
                snackbarHostState.showSnackbar("Profile updated successfully!")
                userViewModel.resetUpdateStatus()
                onUpdateSuccess()
            }
            is UpdateStatus.ReAuthRequired -> {
                isUpdating = false
                val message: String
                when (status.providerId) {
                    ReAuthCodes.PASSWORD_REAUTH_REQUIRED -> {
                        message = "SECURITY WARNING: Please re-enter your password to confirm the email change."
                        // NOTE: You would show a password re-authentication dialog here.
                    }
                    ReAuthCodes.GOOGLE_REAUTH_REQUIRED -> {
                        message = "SECURITY WARNING: Please re-authenticate via Google to confirm the email change."
                        onGoogleReAuthRequired() // Trigger the platform-specific Google login intent
                    }
                    else -> {
                        message = "Re-authentication required for email change. Provider not supported."
                    }
                }
                snackbarHostState.showSnackbar(message)
                userViewModel.resetUpdateStatus()
            }
            is UpdateStatus.Error -> {
                isUpdating = false
                snackbarHostState.showSnackbar(status.message)
                userViewModel.resetUpdateStatus()
            }
        }
    }

    // --- Image Picker Launcher ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profilePictureUri = uri
        }
    }

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
                    .padding(horizontal = 30.dp,vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Back Arrow
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // --- Profile Picture Section ---
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                ) {
                    val imageModifier = Modifier.fillMaxSize()

                    // Use AsyncImage to display the profile picture from URI/URL
                    if (profilePictureUri != null) {
                        AsyncImage(
                            model = profilePictureUri,
                            contentDescription = "Profile Picture",
                            modifier = imageModifier.clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Placeholder",
                            tint = Color.Gray,
                            modifier = imageModifier.padding(16.dp)
                        )
                    }

                    // Edit button overlay
                    FloatingActionButton(
                        onClick = {
                            if (!isUpdating) {
                                imagePickerLauncher.launch("image/*")
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(35.dp)
                            .offset(x = -10.dp, y = -10.dp),
                        containerColor = CyanPrimary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Picture",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                // --- End Profile Picture Section ---

                Spacer(Modifier.height(40.dp))

                Text(
                    text = "Update Details",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name:") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isUpdating
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (Optional):") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Phone
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isUpdating
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email:") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Email Icon") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Email
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isUpdating
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!isUpdating) {
                            isUpdating = true
                            scope.launch {
                                // The ViewModel handles the security check and updates the status
                                userViewModel.updateProfile(
                                    newName = name,
                                    newPhone = phone,
                                    newEmail = email,
                                    newPhotoUri = profilePictureUri
                                )
                                // isUpdating will be set to false by the LaunchedEffect when the status changes
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editing Icon",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Update Details",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

            }
        }
    )
}

@Preview
@Composable
fun EditingScreenPreview(){
    EditingScreen(
        onBackClick = {},
        onUpdateSuccess = {},
        onGoogleReAuthRequired = {},
        snackbarHostState = SnackbarHostState()
    )
}