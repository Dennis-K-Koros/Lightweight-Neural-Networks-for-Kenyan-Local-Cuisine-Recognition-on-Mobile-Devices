package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LandingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var onSignInClickCalled = false
    private var onSignUpClickCalled = false

    @Before
    fun setup() {
        onSignInClickCalled = false
        onSignUpClickCalled = false

        composeTestRule.setContent {
            LandingScreen(
                onSignInClick = { onSignInClickCalled = true },
                onSignUpClick = { onSignUpClickCalled = true }
            )
        }
    }

    @Test
    fun landingScreen_allElements_areDisplayed() {
        // Assert that key UI elements are displayed
        composeTestRule.onNodeWithText("NutriScan Kenya").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("App Icon").assertIsDisplayed()
    }

    @Test
    fun landingScreen_signInButton_triggersCallback() {
        // Click the "Log In" button
        composeTestRule.onNodeWithText("Log In").performClick()
        // Verify the callback was called
        assertTrue(onSignInClickCalled)
    }

    @Test
    fun landingScreen_signUpButton_triggersCallback() {
        // Click the "Sign Up" button
        composeTestRule.onNodeWithText("Sign Up").performClick()
        // Verify the callback was called
        assertTrue(onSignUpClickCalled)
    }
}
