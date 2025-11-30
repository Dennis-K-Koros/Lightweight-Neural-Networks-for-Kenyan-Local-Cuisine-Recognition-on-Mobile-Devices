package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.kenyanfoodrecognitionsystem.view_models.ForgotPasswordUiState
import com.example.kenyanfoodrecognitionsystem.view_models.ForgotPasswordViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// We create a fake view model to control the state during testing.
class FakeForgotPasswordViewModel : ForgotPasswordViewModel() {
    val _uiState = MutableStateFlow(ForgotPasswordUiState())
    override val uiState get() = _uiState

    var onConfirmClickCalledInViewModel = false

    override fun onConfirmClick() {
        onConfirmClickCalledInViewModel = true
        // Simulate a successful API call
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            successMessage = "Password reset link sent to your email.",
            emailSent = true
        )
    }

    override fun onEmailChange(newEmail: String) {
        // Simulate the email change
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    override fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}

@RunWith(JUnit4::class)
class ForgotScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeForgotPasswordViewModel
    private var onBackClickCalled = false
    private var onConfirmClickCalled = false

    @Before
    fun setup() {
        onBackClickCalled = false
        onConfirmClickCalled = false
        fakeViewModel = FakeForgotPasswordViewModel()

        composeTestRule.setContent {
            // We provide our fake ViewModel for testing
            ForgotScreen(
                onBackClick = { onBackClickCalled = true },
                onConfirmClick = { onConfirmClickCalled = true },
                viewModel = fakeViewModel
            )
        }
    }

    @Test
    fun forgotScreen_allElements_areDisplayed() {
        // Check for the presence of key UI elements
        composeTestRule.onNodeWithText("Forgot Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please either input your email or phone number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email/Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun forgotScreen_emailInput_acceptsText() {
        val testEmail = "test@example.com"
        composeTestRule.onNodeWithText("Email/Phone").performTextInput(testEmail)

        // Verify that the ViewModel's state was updated
        composeTestRule.runOnIdle {
            assertTrue(fakeViewModel.uiState.value.email == testEmail)
        }
    }

    @Test
    fun forgotScreen_backButton_triggersCallback() {
        // Click the back arrow icon button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        // Verify the callback was triggered
        assertTrue(onBackClickCalled)
    }

    @Test
    fun forgotScreen_confirmButton_triggersViewModelAction() {
        // Click the Confirm button
        composeTestRule.onNodeWithText("Confirm").performClick()

        // Verify that the onConfirmClick function was called in the ViewModel
        composeTestRule.runOnIdle {
            assertTrue(fakeViewModel.onConfirmClickCalledInViewModel)
        }
    }

    @Test
    fun forgotScreen_confirmButton_showsProgressIndicator_whenLoading() {
        composeTestRule.runOnIdle {
            // Manually set the state to loading
            fakeViewModel._uiState.value = fakeViewModel._uiState.value.copy(isLoading = true)
        }
        // Assert that the progress indicator is displayed and the button text is gone
        composeTestRule.onNodeWithContentDescription("loading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()
    }

    @Test
    fun forgotScreen_buttonText_changesAndNavigates_onSuccess() {
        // Click the Confirm button to simulate a successful request
        composeTestRule.onNodeWithText("Confirm").performClick()

        // Verify that the button text has changed after a "successful" operation
        composeTestRule.onNodeWithText("Back To Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()

        // Click the new "Back To Sign In" button
        composeTestRule.onNodeWithText("Back To Sign In").performClick()

        // Verify the navigation callback was triggered
        assertTrue(onConfirmClickCalled)
    }

    @Test
    fun forgotScreen_errorMessage_isDisplayed() {
        val errorMessage = "Invalid email."
        composeTestRule.runOnIdle {
            fakeViewModel._uiState.value = fakeViewModel._uiState.value.copy(errorMessage = errorMessage)
        }
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun forgotScreen_successMessage_isDisplayed() {
        val successMessage = "Success!"
        composeTestRule.runOnIdle {
            fakeViewModel._uiState.value = fakeViewModel._uiState.value.copy(successMessage = successMessage)
        }
        composeTestRule.onNodeWithText(successMessage).assertIsDisplayed()
    }
}
