package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

    // This rule allows us to set Compose content and interact with it.
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signUpScreen_allElementsAreDisplayed() {
        // Start the test by setting the content to your Composable.
        composeTestRule.setContent {
            // We need to provide the snackbarHostState since it's a parameter.
            val snackbarHostState = remember { SnackbarHostState() }
            SignUpScreen(
                onBackClick = {},
                onSignInClick = {},
                onSignUpClick = { _, _, _, _ -> },
                onPasswordMismatch = {},
                snackbarHostState = snackbarHostState
            )
        }

        // Verify that the key UI elements are displayed on the screen.
        composeTestRule.onNodeWithText("Create An Account").assertExists()
        composeTestRule.onNodeWithText("Name").assertExists()
        composeTestRule.onNodeWithText("Phone").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Confirm Password").assertExists()
        composeTestRule.onNodeWithText("Sign Up").assertExists()
        composeTestRule.onNodeWithText("Sign In").assertExists()
    }

    @Test
    fun signUpScreen_canInputText() {
        // Set up the screen
        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            SignUpScreen(
                onBackClick = {},
                onSignInClick = {},
                onSignUpClick = { _, _, _, _ -> },
                onPasswordMismatch = {},
                snackbarHostState = snackbarHostState
            )
        }

        // Simulate typing into each text field.
        composeTestRule.onNodeWithText("Name").performTextInput("John Doe")
        composeTestRule.onNodeWithText("Phone").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Email").performTextInput("john.doe@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password123")

        // Assert that the text fields now contain the typed text.
        composeTestRule.onNodeWithText("John Doe").assertExists()
        composeTestRule.onNodeWithText("1234567890").assertExists()
        composeTestRule.onNodeWithText("john.doe@example.com").assertExists()
        // Note: Password fields use visual transformation, so we can't check for "password123" directly.
    }

    @Test
    fun signUpScreen_correctPassword_callsOnSignUpClick() = runTest {
        var signUpCalled = false
        var name = ""
        var phone = ""
        var email = ""
        var password = ""

        // Set up the screen with a mock onSignUpClick function.
        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            SignUpScreen(
                onBackClick = {},
                onSignInClick = {},
                onSignUpClick = { n, p, e, pwd ->
                    signUpCalled = true
                    name = n
                    phone = p
                    email = e
                    password = pwd
                },
                onPasswordMismatch = {},
                snackbarHostState = snackbarHostState
            )
        }

        // Perform the actions to fill out the form.
        composeTestRule.onNodeWithText("Name").performTextInput("Jane Smith")
        composeTestRule.onNodeWithText("Phone").performTextInput("0987654321")
        composeTestRule.onNodeWithText("Email").performTextInput("jane.smith@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("securepassword")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("securepassword")

        // Click the Sign Up button.
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Assert that the onSignUpClick function was called with the correct data.
        assert(signUpCalled)
        assert(name == "Jane Smith")
        assert(phone == "0987654321")
        assert(email == "jane.smith@example.com")
        assert(password == "securepassword")
    }

    @Test
    fun signUpScreen_passwordMismatch_callsOnPasswordMismatch() = runTest {
        var mismatchCalled = false

        // Set up the screen with a mock onPasswordMismatch function.
        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            SignUpScreen(
                onBackClick = {},
                onSignInClick = {},
                onSignUpClick = { _, _, _, _ -> },
                onPasswordMismatch = { mismatchCalled = true },
                snackbarHostState = snackbarHostState
            )
        }

        // Fill out the form with mismatched passwords.
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("notmatching")

        // Click the Sign Up button.
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Assert that the onPasswordMismatch function was called.
        assert(mismatchCalled)
    }

    @Test
    fun signUpScreen_backArrowClick_callsOnBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            SignUpScreen(
                onBackClick = { backClicked = true },
                onSignInClick = {},
                onSignUpClick = { _, _, _, _ -> },
                onPasswordMismatch = {},
                snackbarHostState = snackbarHostState
            )
        }

        // The back arrow doesn't have a text, so we use its content description.
        composeTestRule.onNodeWithText("Back").performClick()

        assert(backClicked)
    }

    @Test
    fun signUpScreen_signInTextClick_callsOnSignInClick() {
        var signInClicked = false

        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            SignUpScreen(
                onBackClick = {},
                onSignInClick = { signInClicked = true },
                onSignUpClick = { _, _, _, _ -> },
                onPasswordMismatch = {},
                snackbarHostState = snackbarHostState
            )
        }

        composeTestRule.onNodeWithText("Sign In").performClick()

        assert(signInClicked)
    }
}
