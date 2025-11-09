package com.example.kenyanfoodrecognitionsystem.authentication.view_models

import com.example.kenyanfoodrecognitionsystem.view_models.ForgotPasswordViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private lateinit var viewModel: ForgotPasswordViewModel
    private val auth: FirebaseAuth = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // We no longer mock the static Firebase.auth call.
        // Instead, we pass our mock 'auth' instance directly to the ViewModel's constructor.
        viewModel = ForgotPasswordViewModel(auth = auth)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when onConfirmClick is called with an empty email, an error message is set`() = runTest {
        // Given
        viewModel.onEmailChange("")

        // When
        viewModel.onConfirmClick()

        // Then
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals("Email cannot be empty.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `when onConfirmClick is called with a valid email, success message is set`() = runTest {
        // Mock a successful password reset task
        val mockTask: Task<Void> = mockk(relaxed = true)
        every { auth.sendPasswordResetEmail(any()) } returns mockTask

        // Simulate the completion of the Firebase task successfully
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<Void>
            every { mockTask.isSuccessful } returns true
            every { mockTask.exception } returns null
            listener.onComplete(mockTask)
            mockTask
        }

        // Given
        val validEmail = "test@example.com"
        viewModel.onEmailChange(validEmail)

        // When
        viewModel.onConfirmClick()

        // Then
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
