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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kenyanfoodrecognitionsystem.screens.HomeScreen.BottomNavBar
import com.example.kenyanfoodrecognitionsystem.screens.HomeScreen.OrangeAccent

//Custom colors
val Blue = Color(0xB32F80ED)

@Composable
fun SettingsScreen(){

    var currentView by remember { mutableStateOf("settings") }

    Scaffold(
        bottomBar = { BottomNavBar(currentView) { currentView = it } }
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
                        .width(384.dp)
                        .height(99.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                        Column(
                            modifier = Modifier.padding(vertical = 5.dp),
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            Text(
                                text = "Osti Sangutet",
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            Text(
                                text = "ostisangutet@gmail.com",
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(120.dp))

                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Profile",
                            modifier = Modifier.size(30.dp),
                            tint = Color.White
                        )
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
                        .width(384.dp)
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
                        Surface(
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = 3.dp,
                                color = Blue
                            ),
                            modifier = Modifier
                                .width(90.dp)
                                .height(25.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Default",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "ArrowDown",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                }
            }

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
                        .width(384.dp)
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
                        .width(384.dp)
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
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "ArrowDown",
                                modifier = Modifier.size(24.dp)
                            )
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
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "ArrowDown",
                                modifier = Modifier.size(24.dp)
                            )
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
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "ArrowDown",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
//    SettingsScreen()
}