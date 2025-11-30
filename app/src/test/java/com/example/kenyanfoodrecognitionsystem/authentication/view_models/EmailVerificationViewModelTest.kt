package com.example.kenyanfoodrecognitionsystem.authentication.view_models

import android.util.Log
import com.example.kenyanfoodrecognitionsystem.view_models.EmailVerificationViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class EmailVerificationViewModelTest {

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var viewModel: EmailVerificationViewModel
    private lateinit var mockedLog: MockedStatic<Log>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockedLog = Mockito.mockStatic(Log::class.java)
        viewModel = EmailVerificationViewModel(mockFirebaseAuth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedLog.close()
    }

    @Test
    fun `onResendClick sets isResending to true then false on success`() = runTest {
        // Mock a successful task for sending the email
        val mockResendTask = Mockito.mock(Task::class.java) as Task<Void>
        Mockito.`when`(mockResendTask.isSuccessful).thenReturn(true)
        Mockito.doAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<Void>>(0)
            listener.onComplete(mockResendTask)
            mockResendTask
        }.`when`(mockResendTask).addOnCompleteListener(Mockito.any())

        // Mock the current user to be non-null and return the mock task
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        Mockito.`when`(mockFirebaseUser.sendEmailVerification()).thenReturn(mockResendTask)

        // Trigger the function
        viewModel.onResendClick()

        // Assert the final state
        TestCase.assertFalse(viewModel.uiState.value.isResending)
        TestCase.assertNotNull(viewModel.uiState.value.successMessage)
        TestCase.assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onResendClick sets error message on failure`() = runTest {
        // Mock a failed task for sending the email
        val mockResendTask = Mockito.mock(Task::class.java) as Task<Void>
        Mockito.`when`(mockResendTask.isSuccessful).thenReturn(false)
        Mockito.`when`(mockResendTask.exception).thenReturn(Exception("Test failure"))
        Mockito.doAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<Void>>(0)
            listener.onComplete(mockResendTask)
            null
        }.`when`(mockResendTask).addOnCompleteListener(Mockito.any())

        // Mock the current user to be non-null and return the mock task
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        Mockito.`when`(mockFirebaseUser.sendEmailVerification()).thenReturn(mockResendTask)

        // Trigger the function
        viewModel.onResendClick()

        // Assert the final state
        TestCase.assertFalse(viewModel.uiState.value.isResending)
        TestCase.assertNotNull(viewModel.uiState.value.errorMessage)
        TestCase.assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `onContinueClick updates state to verified on success`() = runTest {
        // Mock a successful reload task
        val mockReloadTask = Mockito.mock(Task::class.java) as Task<Void>
        Mockito.`when`(mockReloadTask.isSuccessful).thenReturn(true)
        Mockito.doAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<Void>>(0)
            listener.onComplete(mockReloadTask)
            mockReloadTask
        }.`when`(mockReloadTask).addOnCompleteListener(Mockito.any())

        // Mock a user with a verified email after reload
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        Mockito.`when`(mockFirebaseUser.reload()).thenReturn(mockReloadTask)
        Mockito.`when`(mockFirebaseUser.isEmailVerified).thenReturn(true)

        // Trigger the function
        viewModel.onContinueClick()

        // Assert the final state
        TestCase.assertTrue(viewModel.uiState.value.isVerified)
        TestCase.assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onContinueClick sets error message if email is not verified`() = runTest {
        // Mock a successful reload task
        val mockReloadTask = Mockito.mock(Task::class.java) as Task<Void>
        Mockito.`when`(mockReloadTask.isSuccessful).thenReturn(true)
        Mockito.doAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<Void>>(0)
            listener.onComplete(mockReloadTask)
            null
        }.`when`(mockReloadTask).addOnCompleteListener(Mockito.any())

        // Mock an unverified email after reload
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        Mockito.`when`(mockFirebaseUser.reload()).thenReturn(mockReloadTask)
        Mockito.`when`(mockFirebaseUser.isEmailVerified).thenReturn(false)

        // Trigger the function
        viewModel.onContinueClick()

        // Assert the final state
        TestCase.assertFalse(viewModel.uiState.value.isLoading)
        TestCase.assertFalse(viewModel.uiState.value.isVerified)
        TestCase.assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onResendClick sets error state when user is null`() = runTest {
        // Mock currentUser to be null
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Trigger the function
        viewModel.onResendClick()

        // Assert the final state
        TestCase.assertFalse(viewModel.uiState.value.isResending)
        TestCase.assertNotNull(viewModel.uiState.value.errorMessage)
        TestCase.assertNull(viewModel.uiState.value.successMessage)
        TestCase.assertEquals("No authenticated user found. Please try logging in again.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onContinueClick sets error state when user is null`() = runTest {
        // Mock currentUser to be null
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Trigger the function
        viewModel.onContinueClick()

        // Assert the final state
        TestCase.assertFalse(viewModel.uiState.value.isLoading)
        TestCase.assertNotNull(viewModel.uiState.value.errorMessage)
        TestCase.assertFalse(viewModel.uiState.value.isVerified)
        TestCase.assertEquals("No authenticated user found.", viewModel.uiState.value.errorMessage)
    }

}
