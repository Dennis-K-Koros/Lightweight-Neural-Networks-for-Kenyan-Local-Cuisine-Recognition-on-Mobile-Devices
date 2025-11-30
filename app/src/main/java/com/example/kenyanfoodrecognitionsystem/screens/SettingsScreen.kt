package com.example.kenyanfoodrecognitionsystem.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kenyanfoodrecognitionsystem.screens.Homescreen.BackgroundLight
import com.example.kenyanfoodrecognitionsystem.screens.Homescreen.CaptureOptionsDialog
import com.example.kenyanfoodrecognitionsystem.view_models.DeleteAccountStatus
import com.example.kenyanfoodrecognitionsystem.view_models.ReAuthCodes
import com.example.kenyanfoodrecognitionsystem.view_models.UserViewModel
import kotlinx.coroutines.launch

//Custom colors
val Blue = Color(0xB32F80ED)
val OrangeAccent = Color(0xFFFF8B40)
val CyanPrimary = Color(0xFF10DAE9)


// --- Navigation Destinations for the App NavGraph ---
const val HOME_ROUTE = "HomeScreen"
const val SETTINGS_ROUTE = "SettingsScreen"
const val EDIT_ROUTE = "EditingScreen"
const val PASSWORD_ROUTE = "PasswordChangeScreen"
const val LANDING_ROUTE = "LandingScreen"


// Add this enum at the top level (outside the composable)
enum class AppTheme(val displayName: String) {
    DEFAULT("Default"),
    DARK("Dark"),
    DEVICE("Device")
}

@Composable
fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 3.dp,
                color = Blue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = 3.dp,
                            color = Blue
                        ),
                        modifier = Modifier
                            .width(90.dp)
                            .height(25.dp)
                            .clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTheme.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Select Theme",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(120.dp)
                    ) {
                        AppTheme.values().forEach { theme ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = theme.displayName,
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    onThemeSelected(theme)
                                    expanded = false
                                },
                                modifier = Modifier.background(
                                    if (theme == selectedTheme) Blue.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    userViewModel: UserViewModel = viewModel()
){

    // 1. Observe the user state from the ViewModel
    val user by userViewModel.currentUser.collectAsState()
    val userName = user?.name ?: "johnDoe"
    val userEmail = user?.email ?: "johndoe@gmail.com"
    val userProfileImageUrl = user?.profileImageUrl

    // State management for UI interactions
    var selectedTheme by remember { mutableStateOf(AppTheme.DEFAULT) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeletePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    val deleteAccountStatus by userViewModel.deleteAccountStatus.collectAsState()

    // Handle delete account status changes
    LaunchedEffect(deleteAccountStatus) {
        when (deleteAccountStatus) {
            is DeleteAccountStatus.Success -> {
                // Account deleted successfully, navigate to login/welcome screen
                // onNavigate("login") // Add your login route
                userViewModel.resetDeleteAccountStatus()
            }
            is DeleteAccountStatus.ReAuthRequired -> {
                val status = deleteAccountStatus as DeleteAccountStatus.ReAuthRequired
                if (status.providerId == ReAuthCodes.PASSWORD_REAUTH_REQUIRED) {
                    showDeletePasswordDialog = true
                } else if (status.providerId == ReAuthCodes.GOOGLE_REAUTH_REQUIRED) {
                    // Handle Google re-auth (implement in your UI layer)
                    // Show appropriate dialog or trigger Google Sign-In
                }
            }
            is DeleteAccountStatus.Error -> {
                val error = (deleteAccountStatus as DeleteAccountStatus.Error).message
                // Show error to user (you can use a Snackbar or Toast)
                println("Delete account error: $error")
            }
            DeleteAccountStatus.Idle -> { /* Do nothing */ }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            BottomNavBar(
                onNavigate = onNavigate,
                currentRoute = SETTINGS_ROUTE
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ){
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                modifier = Modifier.padding(horizontal = 15.dp)
            )
            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                Surface (
                    shape = RoundedCornerShape(25),
                    color = Blue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(99.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            val imageModifier = Modifier.fillMaxSize()

                            // Use AsyncImage to display the profile picture from URI/URL
                            if (userProfileImageUrl != null) {
                                AsyncImage(
                                    model = userProfileImageUrl,
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
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .weight(1f), // This makes the column take up remaining space
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis // Handle very long names
                            )

                            Text(
                                text = userEmail,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis // Handle very long emails
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp)) // Small fixed spacing before edit button

                        IconButton(
                            onClick = { onNavigate(EDIT_ROUTE) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "Customization",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 15.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ThemeSelector(
                selectedTheme = selectedTheme,
                onThemeSelected = { theme ->
                    selectedTheme = theme
                    // TODO: Apply the theme to your app
                    // You can call userViewModel.updateTheme(theme) or similar
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Legal",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 15.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(25),
                    color = Blue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(99.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceAround,

                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 15.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Terms and Conditions",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "ArrowDown",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 15.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "ArrowDown",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 15.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(25),
                    border = BorderStroke(
                        width = 3.dp,
                        color = Blue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(139.dp)
                ){
                    Column(
                        verticalArrangement = Arrangement.SpaceAround,
                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 15.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = "Change Password",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { onNavigate(PASSWORD_ROUTE) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "ArrowDown"
                                )
                            }

                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 15.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = "Log Out",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "ArrowDown"
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 15.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = "Delete Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "ArrowDown"
                                )
                            }

                        }
                    }

                }
            }


        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = {
                Text(
                    "Are you sure you want to delete your account? This action cannot be undone. All your data will be permanently deleted.",
                    color = Color.Red
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        userViewModel.deleteAccount()
                        onNavigate(LANDING_ROUTE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = BackgroundLight
        )
    }

    // Re-authentication Password Dialog
    if (showDeletePasswordDialog) {
        val coroutineScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = {
                showDeletePasswordDialog = false
                userViewModel.resetDeleteAccountStatus()
            },
            title = { Text("Confirm Your Password") },
            text = {
                Column {
                    Text("For security, please enter your password to continue.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result =
                                userViewModel.reAuthenticateWithPassword(
                                    deletePassword
                                )
                            if (result.isSuccess) {
                                showDeletePasswordDialog = false
                                deletePassword = ""
                                userViewModel.deleteAccount() // Retry deletion
                            } else {
                                // Show error
                                println("Re-auth failed: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeletePasswordDialog = false
                    deletePassword = ""
                    userViewModel.resetDeleteAccountStatus()
                }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = BackgroundLight
        )
    }

    // Log Out Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = {
                Text("Are you sure you want to log out?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        userViewModel.signOut()
                        onNavigate(LANDING_ROUTE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = BackgroundLight
        )
    }

}


/**
 * Floating, center-aligned bottom navigation bar with a large FAB.
 */
@Composable
fun BottomNavBar(onNavigate: (String) -> Unit,currentRoute: String) {
    var showCaptureOptions by remember { mutableStateOf(false) }
    val navBarHeight = 85.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(navBarHeight + 40.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarHeight),
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
            color = Color(0xAD10DAE9),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    isActive = currentRoute == HOME_ROUTE,
                    onClick = { onNavigate(HOME_ROUTE) }
                )

                Spacer(modifier = Modifier.width(60.dp))

                NavItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    isActive = currentRoute == SETTINGS_ROUTE,
                    onClick = { onNavigate(SETTINGS_ROUTE) }
                )
            }
        }

        // Floating Action Button (FAB) - Camera icon
        FloatingActionButton(
            onClick = { showCaptureOptions = true },
            shape = CircleShape,
            containerColor = Color.Black,
            modifier = Modifier
                .offset(y = (-40).dp)
                .size(84.dp)
        ) {
            Icon(
                Icons.Filled.CameraAlt,
                contentDescription = "Capture Food",
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
        }
    }

    if (showCaptureOptions) {
        CaptureOptionsDialog(
            onDismiss = { showCaptureOptions = false },
            onCamera = {
                showCaptureOptions = false
                onNavigate("CaptureScreen/camera")
            },
            onGallery = {
                showCaptureOptions = false
                onNavigate("CaptureScreen/gallery")
            },
            onTextInput = {
                showCaptureOptions = false
                onNavigate("TextInputScreen")
            }
        )
    }
}

/**
 * Helper Composable for individual Nav Bar items.
 */
@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        val color = if (isActive) OrangeAccent else Color.White
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(45.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Dialog that pops up when the FAB is clicked, offering capture options.
 */
@Composable
fun CaptureOptionsDialog(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onTextInput: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Capture Food") },
        text = { Text("How would you like to input your meal?") },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onCamera,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Take a Picture")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onGallery,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, CyanPrimary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CyanPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Select from Gallery", color = CyanPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onTextInput,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Blue)
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = Blue)
                    Spacer(Modifier.width(8.dp))
                    Text("Discover Through Text Input", color = Blue)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = BackgroundLight
    )


}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
//    SettingsScreen()
}