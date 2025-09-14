package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SignInScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var onBackClickCalled = false
    private var onSignInClickCalled = false
    private var onSignUpClickCalled = false
    private var onForgotClickCalled = false
    private var snackbarHostState: SnackbarHostState? = null

    @Before
    fun setup() {
        onBackClickCalled = false
        onSignInClickCalled = false
        onSignUpClickCalled = false
        onForgotClickCalled = false

        composeTestRule.setContent {
            snackbarHostState = remember { SnackbarHostState() }
            SignInScreen(
                onBackClick = { onBackClickCalled = true },
                onSignInClick = { _, _ -> onSignInClickCalled = true },
                onSignUpClick = { onSignUpClickCalled = true },
                onForgotClick = { onForgotClickCalled = true },
                snackbarHostState = snackbarHostState!!
            )
        }
    }

    @Test
    fun signInScreen_allElements_areDisplayed() {
        // Assert that key UI elements are displayed
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email/Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot password?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log In").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun signInScreen_emailInput_acceptsText() {
        val testEmail = "test@example.com"
        composeTestRule.onNodeWithText("Email/Phone").performTextInput(testEmail)
        composeTestRule.onNodeWithText(testEmail).assertIsDisplayed()
    }

    @Test
    fun signInScreen_passwordInput_acceptsText() {
        val testPassword = "securepassword"
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        // The input is hidden by visual transformation, so we can't directly check the text.
        // We can only check if the field exists and accepts input. The default behavior
        // should be a visual transformation which is handled by the component itself.
        // We will test the visibility toggle separately.
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun signInScreen_passwordVisibilityIcon_togglesVisibility() {
        // The default state is with the password hidden, so the content description should be "Show password"
        val showPasswordIcon = composeTestRule.onNodeWithContentDescription("Show password")
        showPasswordIcon.assertIsDisplayed()

        // Click the icon to make the password visible
        showPasswordIcon.performClick()

        // After the click, the icon should change to hide the password.
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun signInScreen_backButton_triggersCallback() {
        // Click the back arrow
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        // Verify the callback was called
        assertTrue(onBackClickCalled)
    }

    @Test
    fun signInScreen_signUpText_triggersCallback() {
        // Find and click the "Sign up" text
        composeTestRule.onNodeWithText("Sign up").performClick()
        // Verify the callback was called
        assertTrue(onSignUpClickCalled)
    }

    @Test
    fun signInScreen_forgotPasswordText_triggersCallback() {
        // Find and click the "Forgot password?" text
        composeTestRule.onNodeWithText("Forgot password?").performClick()
        // Verify the callback was called
        assertTrue(onForgotClickCalled)
    }

    @Test
    fun signInScreen_signInButton_triggersCallback() {
        val testEmail = "test@example.com"
        val testPassword = "securepassword"

        // Input text into both fields
        composeTestRule.onNodeWithText("Email/Phone").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        // Click the Log In button
        composeTestRule.onNodeWithText("Log In").performClick()

        // Verify the callback was called. We can't check the arguments passed, but we know the function was executed.
        assertTrue(onSignInClickCalled)
    }
}
