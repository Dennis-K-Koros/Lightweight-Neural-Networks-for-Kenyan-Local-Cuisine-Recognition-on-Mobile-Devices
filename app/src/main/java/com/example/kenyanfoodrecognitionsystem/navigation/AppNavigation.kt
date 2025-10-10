package com.example.kenyanfoodrecognitionsystem.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kenyanfoodrecognitionsystem.authentication.AuthManager
import com.example.kenyanfoodrecognitionsystem.authentication.EditingScreen
import com.example.kenyanfoodrecognitionsystem.authentication.EmailVerificationScreen
import com.example.kenyanfoodrecognitionsystem.authentication.ForgotScreen
import com.example.kenyanfoodrecognitionsystem.authentication.LandingScreen
import com.example.kenyanfoodrecognitionsystem.authentication.PasswordChangeScreen
import com.example.kenyanfoodrecognitionsystem.authentication.SignInScreen
import com.example.kenyanfoodrecognitionsystem.authentication.SignUpScreen
import com.example.kenyanfoodrecognitionsystem.authentication.UserVerificationScreen
import com.example.kenyanfoodrecognitionsystem.data_models.User
import com.example.kenyanfoodrecognitionsystem.screens.Homescreen.HomeScreen
import com.example.kenyanfoodrecognitionsystem.screens.SettingsScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(){

    val navController = rememberNavController()
    val auth = AuthManager.auth
    val firestore = Firebase.firestore

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "landingScreen"
    ){
        //Landing Screen
        composable("landingScreen"){

            LandingScreen(
                onSignInClick = { navController.navigate("SignInScreen") },
                onSignUpClick = { navController.navigate("SignUpScreen") }
            )

        }

        //SignIn Screen
        composable("SignInScreen"){
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = {navController.navigate("SignUpScreen")},
                onForgotClick = {navController.navigate("ForgotScreen")},
                onSignInClick = {email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("HomeScreen") {
                                    // Clear the back stack so the user can't navigate back to sign-in
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            }else{
                                // Handle the failure case
                                coroutineScope.launch {
                                    val errorMessage = task.exception?.message ?: "An unknown error occurred."
                                    snackbarHostState.showSnackbar(
                                        message = "Login Failed: $errorMessage"
                                    )
                                }
                            }
                        }

                },
                onGoogleSignInSuccess = {navController.navigate("HomeScreen")},
                snackbarHostState = snackbarHostState // Pass the state to the screen
            )
        }

        //SignUp Screen
        composable("SignUpScreen") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = {navController.navigate("SignInScreen")},
                onPasswordMismatch = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Passwords do not match."
                        )
                    }
                },
                onSignUpClick = {name, phone, email, password -> // The hoisted data is received here
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser

                                // The user's UID is a property of the user object
                                val uid = user?.uid

                                if (uid != null) {
                                    val userModel = User(
                                        uid = uid,
                                        name = name,
                                        email = email,
                                        phone = phone
                                    )

                                    // Use .document(uid) to set the document ID
                                    firestore.collection("users").document(uid).set(userModel)
                                        .addOnSuccessListener {
                                            println("User data saved successfully to Firestore.")
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error saving user data: $e")
                                        }

                                }
                                // STEP 1: Send the email verification
                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener { emailTask ->
                                        if (!emailTask.isSuccessful) {
                                            // Handle the rare case where sending the email fails
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Failed to send verification email. Please try again."
                                                )
                                            }
                                        }
                                    }
                                navController.navigate("EmailVerificationScreen")

                            } else {
                                // Handle sign-up failure by showing a Snackbar
                                coroutineScope.launch {
                                    val errorMessage = when (task.exception) {
                                        is FirebaseAuthWeakPasswordException -> "Password is too weak. It must be at least 6 characters."
                                        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                                        else -> "Sign-up failed: ${task.exception?.message ?: "Unknown error"}"
                                    }
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage
                                    )
                                }
                            }

                        }
                },
                onGoogleSignInSuccess = {navController.navigate("HomeScreen")},
                snackbarHostState = snackbarHostState
            )
        }

        //Forgot Screen
        composable("ForgotScreen"){
            ForgotScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = {navController.navigate("SignInScreen")}
            )
        }

        //Email Verification Screen
        composable("EmailVerificationScreen"){
            EmailVerificationScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = {navController.navigate("SignInScreen")}
            )
        }

        //User Verification Screen
        composable("UserVerificationScreen"){
            UserVerificationScreen(
                onBackClick = { navController.popBackStack() },
                onResendClick = {},
                onConfirmClick = {},
                onOtpTextChange = {}
            )
        }

        //Password Change Screen
        composable("PasswordChangeScreen"){
            PasswordChangeScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = {navController.navigate("SettingsScreen")}
            )
        }

        //HomeScreen
        composable("HomeScreen"){
            HomeScreen(
                onNavigate = {route -> navController.navigate(route)}
            )
        }

        //Settings Screen
        composable("SettingsScreen"){
            SettingsScreen(
                onNavigate = {route -> navController.navigate(route)}
            )
        }

        //Editing Screen
        composable("EditingScreen"){
            EditingScreen(
                onBackClick = { navController.popBackStack()},
                onUpdateSuccess = {navController.navigate("SettingsScreen")},
                onGoogleReAuthRequired = {},
                snackbarHostState = snackbarHostState
            )
        }
    }
}

