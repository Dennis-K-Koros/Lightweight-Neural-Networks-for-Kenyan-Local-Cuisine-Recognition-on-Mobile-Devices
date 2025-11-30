package com.example.kenyanfoodrecognitionsystem.authentication.view_models

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kenyanfoodrecognitionsystem.view_models.GoogleAuthUiState
import com.example.kenyanfoodrecognitionsystem.view_models.GoogleAuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleAuthViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockGoogleSignInClient: GoogleSignInClient
    private lateinit var viewModel: GoogleAuthViewModel
    private lateinit var googleAuthProviderMock: MockedStatic<GoogleAuthProvider>

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockAuth = mock()
        mockGoogleSignInClient = mock()

        googleAuthProviderMock = mockStatic(GoogleAuthProvider::class.java)

        // Stub the signInIntent
        doReturn(mock<Intent>()).`when`(mockGoogleSignInClient).signInIntent

        viewModel = GoogleAuthViewModel(mockAuth, mockGoogleSignInClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        googleAuthProviderMock.close()
    }

    // --- Helper functions for mocking Firebase Tasks ---
    private fun <T> createSuccessfulTask(result: T?): Task<T> {
        val task: Task<T> = mock()

        doReturn(true).`when`(task).isSuccessful
        doReturn(result).`when`(task).result
        doReturn(true).`when`(task).isComplete
        doReturn(null).`when`(task).exception
        doReturn(false).`when`(task).isCanceled

        doAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnCompleteListener<T>>(0)
            listener.onComplete(task)
            task
        }.`when`(task).addOnCompleteListener(any())

        doAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnSuccessListener<T>>(0)
            result?.let { listener.onSuccess(it) }
            task
        }.`when`(task).addOnSuccessListener(any())

        doReturn(task).`when`(task).addOnFailureListener(any())

        return task
    }

    private fun <T> createFailureTask(exception: Exception): Task<T> {
        val task: Task<T> = mock()

        doReturn(false).`when`(task).isSuccessful
        doReturn(exception).`when`(task).exception
        doReturn(true).`when`(task).isComplete
        doReturn(false).`when`(task).isCanceled

        doAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnCompleteListener<T>>(0)
            listener.onComplete(task)
            task
        }.`when`(task).addOnCompleteListener(any())

        doReturn(task).`when`(task).addOnSuccessListener(any())

        doAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
            listener.onFailure(exception)
            task
        }.`when`(task).addOnFailureListener(any())

        return task
    }

    @Test
    fun `signInWithGoogleToken_Success_ExistingUser`() = runTest {
        val idToken = "fake-token"

        // Mock the credential
        val mockCredential = mock<com.google.firebase.auth.AuthCredential>()
        googleAuthProviderMock.`when`<com.google.firebase.auth.AuthCredential> {
            GoogleAuthProvider.getCredential(idToken, null)
        }.thenReturn(mockCredential)

        // Mock the AuthResult components
        val mockResult: AuthResult = mock()
        val mockUser: FirebaseUser = mock()
        val mockAdditionalInfo: AdditionalUserInfo = mock()

        doReturn(false).`when`(mockAdditionalInfo).isNewUser
        doReturn("Test User").`when`(mockUser).displayName
        doReturn("test@example.com").`when`(mockUser).email
        doReturn(mockUser).`when`(mockResult).user
        doReturn(mockAdditionalInfo).`when`(mockResult).additionalUserInfo

        // Use doReturn().when() instead of whenever()
        val task = createSuccessfulTask(mockResult)
        doReturn(task).`when`(mockAuth).signInWithCredential(mockCredential)

        // When
        viewModel.signInWithGoogleToken(idToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.error)
        assertEquals("Test User", viewModel.uiState.value.userDisplayName)
        assertEquals("test@example.com", viewModel.uiState.value.userEmail)
        assertFalse(viewModel.uiState.value.isNewUser)
    }

    @Test
    fun `signInWithGoogleToken_Success_NewUser`() = runTest {
        val idToken = "fake-token"

        val mockCredential = mock<com.google.firebase.auth.AuthCredential>()
        googleAuthProviderMock.`when`<com.google.firebase.auth.AuthCredential> {
            GoogleAuthProvider.getCredential(idToken, null)
        }.thenReturn(mockCredential)

        val mockResult: AuthResult = mock()
        val mockUser: FirebaseUser = mock()
        val mockAdditionalInfo: AdditionalUserInfo = mock()

        doReturn(true).`when`(mockAdditionalInfo).isNewUser
        doReturn("New User").`when`(mockUser).displayName
        doReturn("newuser@example.com").`when`(mockUser).email
        doReturn(mockUser).`when`(mockResult).user
        doReturn(mockAdditionalInfo).`when`(mockResult).additionalUserInfo

        val task = createSuccessfulTask(mockResult)
        doReturn(task).`when`(mockAuth).signInWithCredential(mockCredential)

        // When
        viewModel.signInWithGoogleToken(idToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.isNewUser)
    }

    @Test
    fun `signInWithGoogleToken_Failure_AuthenticationError`() = runTest {
        val idToken = "fake-token"
        val expectedErrorMessage = "Invalid credentials"

        val mockCredential = mock<com.google.firebase.auth.AuthCredential>()
        googleAuthProviderMock.`when`<com.google.firebase.auth.AuthCredential> {
            GoogleAuthProvider.getCredential(idToken, null)
        }.thenReturn(mockCredential)

        val exception = Exception(expectedErrorMessage)
        val task = createFailureTask<AuthResult>(exception)
        doReturn(task).`when`(mockAuth).signInWithCredential(mockCredential)

        // When
        viewModel.signInWithGoogleToken(idToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error?.contains(expectedErrorMessage) ?: false)
    }

    @Test
    fun `signInWithGoogleToken_Failure_NetworkError`() = runTest {
        val idToken = "fake-token"

        val mockCredential = mock<com.google.firebase.auth.AuthCredential>()
        googleAuthProviderMock.`when`<com.google.firebase.auth.AuthCredential> {
            GoogleAuthProvider.getCredential(idToken, null)
        }.thenReturn(mockCredential)

        val exception = IOException("Network failed")
        val task = createFailureTask<AuthResult>(exception)
        doReturn(task).`when`(mockAuth).signInWithCredential(mockCredential)

        // When
        viewModel.signInWithGoogleToken(idToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error?.contains("Network failed") ?: false)
    }

    @Test
    fun `signInWithGoogleToken_Failure_NullUser`() = runTest {
        val idToken = "fake-token"

        val mockCredential = mock<com.google.firebase.auth.AuthCredential>()
        googleAuthProviderMock.`when`<com.google.firebase.auth.AuthCredential> {
            GoogleAuthProvider.getCredential(idToken, null)
        }.thenReturn(mockCredential)

        val mockResult: AuthResult = mock()
        doReturn(null).`when`(mockResult).user

        val task = createSuccessfulTask(mockResult)
        doReturn(task).`when`(mockAuth).signInWithCredential(mockCredential)

        // When
        viewModel.signInWithGoogleToken(idToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error?.contains("Firebase sign-in failed unexpectedly") ?: false)
    }

    @Test
    fun `resetState_ClearsUIState`() {
        // Given - set some state first
        viewModel._uiState.value = GoogleAuthUiState(
            isLoading = true,
            error = "Some error",
            isSuccess = true,
            userDisplayName = "Test User"
        )

        // When
        viewModel.resetState()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isSuccess)
        assertNull(state.userDisplayName)
        assertNull(state.userEmail)
        assertFalse(state.isNewUser)
    }

    @Test
    fun `signInWithGoogleToken_SetsLoadingState_Initially`() = runTest {
        val idToken = "fake-token"

        val mockCredential = mock<com.google.firebase.auth.AuthCredential>()
        googleAuthProviderMock.`when`<com.google.firebase.auth.AuthCredential> {
            GoogleAuthProvider.getCredential(idToken, null)
        }.thenReturn(mockCredential)

        val mockResult: AuthResult = mock()
        val mockUser: FirebaseUser = mock()
        doReturn(mockUser).`when`(mockResult).user

        val task = createSuccessfulTask(mockResult)
        doReturn(task).`when`(mockAuth).signInWithCredential(mockCredential)

        // When
        viewModel.signInWithGoogleToken(idToken)

        // Then - before coroutine completes
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSuccess)

        // Clean up
        testDispatcher.scheduler.advanceUntilIdle()
    }
}