package com.example.kenyanfoodrecognitionsystem.screens.Homescreen

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kenyanfoodrecognitionsystem.view_models.UserViewModel
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

// Custom colors based on the image provided
val CyanPrimary = Color(0xFF10DAE9)
val OrangeAccent = Color(0xFFFF8B40)
val BackgroundLight = Color(0xFFFFFFFF)

// --- Navigation Destinations for the App NavGraph ---
const val HOME_ROUTE = "HomeScreen"
const val SETTINGS_ROUTE = "SettingsScreen"
const val CAPTURE_ROUTE = "capture"

/**
 * Main Composable for the Home Screen.
 * @param userName The name of the currently logged-in user.
 * @param onNavigate Lambda function to handle navigation actions (e.g., to Settings or Camera).
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    userViewModel: UserViewModel = viewModel()
) {

    // 1. Observe the user state from the ViewModel
    val user by userViewModel.currentUser.collectAsState()
    val userName = user?.name ?: "JohnDoe"
    val userProfileImageUrl = user?.profileImageUrl

    // State management for UI interactions
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var showCaptureOptions by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            BottomNavBar(
                onNavigate = onNavigate,
                currentRoute = HOME_ROUTE
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                if (user == null && userViewModel.userState.collectAsState().value is com.example.kenyanfoodrecognitionsystem.view_models.UserUiState.Loading) {
                    // Show a simple loading state while waiting for Firebase Auth listener
                    Column(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = CyanPrimary)
                        Spacer(Modifier.height(16.dp))
                        Text("Loading profile...")
                    }
                } else {
                    HomeHeader(userName = userName, userProfileImageUrl=userProfileImageUrl)
                }
            }
            item {
                WeeklyCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> selectedDate = date }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(
                    color = Color(0xFFE0E0E0),
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item { RecentHistoryContent() }
        }
    }

    // Capture Options Dialog
    if (showCaptureOptions) {
        CaptureOptionsDialog(
            onDismiss = { showCaptureOptions = false },
            onCamera = {
                showCaptureOptions = false
                onNavigate(CAPTURE_ROUTE)
            },
            onGallery = {
                showCaptureOptions = false
                // Handle gallery pick action
                // In a real app, this would launch the image picker
            }
        )
    }
}

/**
 * Top section with user greeting and notification icon.
 */
@Composable
fun HomeHeader(userName: String, userProfileImageUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile and Greeting
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
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
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Hello $userName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        // Notification Bell
        IconButton(
            onClick = { /* Handle notification click */ },
            modifier = Modifier
                .size(50.dp)
                .background(Color(0xCC31EDFC), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Horizontal weekly calendar selector component with real dates and functional month picker.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyCalendar(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    // Calculate the start of the week for the selected date (not just today)
    val startOfWeek = remember(selectedDate) {
        selectedDate.minusDays(selectedDate.dayOfWeek.value - 1L)
    }
    val weekDays = remember(startOfWeek) { (0L..6L).map { startOfWeek.plusDays(it) } }

    val nextWeekStart = startOfWeek.plusWeeks(1)
    val isFutureWeek = nextWeekStart.isAfter(today.minusDays(today.dayOfWeek.value.toLong()))

    var expanded by remember { mutableStateOf(false) }
    val currentMonth = selectedDate.month
    val currentYear = selectedDate.year

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Month Selector Dropdown - Functional with current month
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xC6A9A9A9).copy(alpha=0.2f),
                modifier = Modifier.menuAnchor()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Calendar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())} $currentYear",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select Month",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Filter months to prevent selecting a month/year combination in the future
                Month.entries.forEach { month ->
                    // Logic to check if the 1st of this month is equal to or before today
                    val checkDate = LocalDate.of(currentYear, month, 1)
                    val isFutureMonth = checkDate.isAfter(today.withDayOfMonth(1))

                    if (!isFutureMonth) {
                        DropdownMenuItem(
                            text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                            onClick = {
                                // Keep the current year but change the month
                                val newDate = LocalDate.of(currentYear, month, 1)
                                    .withDayOfMonth(minOf(selectedDate.dayOfMonth, month.length(selectedDate.isLeapYear)))
                                onDateSelected(newDate)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Carousel Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Week Button
            IconButton(
                onClick = {
                    val previousWeekDate = selectedDate.minusWeeks(1)
                    onDateSelected(previousWeekDate)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Previous Week",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Week Range Display
            Text(
                text = "${startOfWeek.dayOfMonth} ${startOfWeek.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} - ${weekDays.last().dayOfMonth} ${weekDays.last().month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )

            // Next Week Button
            IconButton(
                onClick = {
                    val nextWeekDate = selectedDate.plusWeeks(1)
                    onDateSelected(nextWeekDate)
                },
                enabled = !isFutureWeek,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Next Week",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Week Day/Date Row - Displaying the current week's dates
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { date ->
                val isSelected = date.isEqual(selectedDate)
                val isToday = date.isEqual(today)
                // Disable dates in the future within the displayed week
                val isClickable = !date.isAfter(today)
                val backgroundColor = when {
                    isSelected -> OrangeAccent
                    isToday -> CyanPrimary.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
                val textColor = Color.Black
                val dayTextColor =Color.Black

                Column(
                    modifier = Modifier
                        .height(92.dp)
                        .width(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable(enabled = isClickable) { onDateSelected(date) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background( Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            fontSize = 16.sp,
                            color = dayTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background( Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 16.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recent Classification History Section - Called within LazyColumn scope.
 */
@Composable
private fun RecentHistoryContent() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            text = "Scan History",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }

    // Mock history items (expandable for real data)
    val historyItems = listOf(
        "Ugali (Dinner)", "Mandazi (Breakfast)", "Samosa (Snack)", "Chips Masala (Lunch)",
        "Githeri (Dinner)", "Mukimo (Lunch)", "Chapati (Breakfast)", "Sukuma Wiki (Dinner)",
        "Pilau (Lunch)", "Bhajia (Snack)", "Nyama Choma (Dinner)", "Matoke (Lunch)","Beans (Dinner)",
        "Kachumbari (Snack)"
    )

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        historyItems.forEach { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanPrimary.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }

    // Add extra padding at the bottom
    Spacer(modifier = Modifier.height(100.dp))
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
            },
            onGallery = {
                showCaptureOptions = false
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
    onGallery: () -> Unit
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

                // Add extra space before the cancel button
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

// --- Preview Composable ---
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    //HomeScreen(userName = "Osti")
}