package com.example.kenyanfoodrecognitionsystem.authentication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EmailVerificationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockViewModel: EmailVerificationViewModel

    @Mock
    private lateinit var mockNavController: NavController

    // A state flow to control the UI state from the test
    private val mockUiStateFlow = MutableStateFlow(EmailVerificationUiState())

    @Before
    fun setUp() {
        // Mock the ViewModel's behavior
        `when`(mockViewModel.uiState).thenReturn(mockUiStateFlow)

        // Set the content of the composable screen for testing
        composeTestRule.setContent {
            EmailVerificationScreen(
                onBackClick = { mockNavController.navigateUp() },
                onContinueClick = { mockNavController.navigate("home_screen_route") },
                viewModel = mockViewModel
            )
        }
    }

    @Test
    fun `resend button calls onResendClick on ViewModel`() {
        // Find the "Resend Link" text and perform a click
        composeTestRule.onNodeWithText("Resend Link").performClick()

        // Verify that the onResendClick method was called on the mocked ViewModel
        verify(mockViewModel).onResendClick()
    }

    @Test
    fun `continue button calls onContinueClick on ViewModel`() {
        // Find the "Confirm" button and perform a click
        composeTestRule.onNodeWithText("Confirm").performClick()

        // Verify that the onContinueClick method was called on the mocked ViewModel
        verify(mockViewModel).onContinueClick()
    }
}
