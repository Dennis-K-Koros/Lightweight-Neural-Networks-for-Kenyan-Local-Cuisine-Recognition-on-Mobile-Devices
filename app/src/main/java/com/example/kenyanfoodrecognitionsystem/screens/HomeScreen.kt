package com.example.kenyanfoodrecognitionsystem.screens.HomeScreen

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Custom colors based on the image provided
val CyanPrimary = Color(0xFF10DAE9) // A bright cyan for the FAB and active nav bar item
val OrangeAccent = Color(0xFFFF8B40) // Orange for the selected calendar date
val BackgroundLight = Color(0xFFFFFFFF)

/**
 * Main Composable for the Home Screen.
 */
@Composable
fun HomeScreen() {
    // State management for UI interactions
    var selectedDate by remember { mutableStateOf(24) }
    var currentView by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = { BottomNavBar(currentView) { currentView = it } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeHeader(userName = "Osti")
            WeeklyCalendar(selectedDate) { date -> selectedDate = date }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 2.dp, modifier = Modifier.padding(horizontal = 20.dp))

            // --- Main Content Area (History Placeholder) ---
            RecentHistorySection()
        }
    }
}

/**
 * Top section with user greeting and notification icon.
 */
@Composable
fun HomeHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile and Greeting
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.padding(15.dp),
                    tint = Color.Black
                )
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
 * Horizontal weekly calendar selector component.
 */
@Composable
fun WeeklyCalendar(selectedDate: Int, onDateSelected: (Int) -> Unit) {
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
    val dates = listOf(22, 23, 24, 25, 26, 27, 28)

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Column() {
            // Month Selector Dropdown - FIXED implementation for pill shape
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable { /* Open month picker */ }
            ) {
                Surface(
                    // Use a large corner radius for a pill shape
                    shape = RoundedCornerShape(50),
                    color = Color(0xC6A9A9A9).copy(alpha = 0.2f),
                ) {
                    Row(
                        // Add padding inside the Surface for content spacing
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
                            text = "September",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Month",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

        }

        // Week Day/Date Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dates.forEachIndexed { index, date ->
                val isSelected = date == selectedDate
                val backgroundColor = if (isSelected) OrangeAccent else Color.Transparent
                val textColor =Color.Black
                val dayTextColor = Color.Black

                Column(
                    modifier = Modifier
                        .height(92.dp)
                        .width(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { onDateSelected(date) }
                        .padding( 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly

                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp) // Adjust size as needed
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center

                    ){
                        Text(
                            text = weekDays[index],
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
                    ){
                        Text(
                            text = date.toString(),
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
 * Placeholder for the Recent Classification History Section.
 */
@Composable
fun RecentHistorySection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            text = "Scan History",
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Mock history items
        val historyItems = listOf("Mandazi (Breakfast)", "Samosa (Snack)","Chips Masala (Lunch)","Ugali (Dinner)")

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
                    // Placeholder for the image thumbnail
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
}

/**
 * Floating, center-aligned bottom navigation bar with a large FAB.
 */
@Composable
fun BottomNavBar(currentView: String, onNavigate: (String) -> Unit) {
    // Custom height to accommodate the floating FAB
    val navBarHeight = 85.dp

    // Custom container to achieve the rounded top and floating effect
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(navBarHeight + 30.dp) // Extra space for FAB float
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        // The main navigation background
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
                // Navigation Items
                NavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    isActive = currentView == "home",
                    onClick = { onNavigate("home") }
                )

                // Placeholder for the FAB space
                Spacer(modifier = Modifier.width(60.dp))

                NavItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    isActive = currentView == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }

        // Floating Action Button (FAB) - Camera icon
        FloatingActionButton(
            onClick = { onNavigate("capture") },
            shape = CircleShape,
            containerColor = Color.Black,
            modifier = Modifier.offset(y = (-40).dp).size(84.dp)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = "Capture Food", modifier = Modifier.size(50.dp),tint = Color.White)
        }
    }
}

/**
 * Helper Composable for individual Nav Bar items.
 */
@Composable
fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
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

// --- Preview Composable ---
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Note: If using AppTheme, you would wrap it here:
    // AppTheme { HomeScreen() }
    //HomeScreen()
}
