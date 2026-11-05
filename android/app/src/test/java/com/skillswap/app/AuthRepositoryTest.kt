package com.skillswap.app

import com.skillswap.app.data.api.ApiService
import com.skillswap.app.data.model.AuthResponse
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.ApiResult
import com.skillswap.app.data.repository.AuthRepository
import com.skillswap.app.util.SessionManager
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class AuthRepositoryTest {

    private val fakeUser = User(
        userId = "u1",
        name = "Asive Homani",
        email = "asive@example.com",
        languagePref = "en",
        timeCredits = 0,
        notificationsEnabled = true,
        syncOnWifiOnly = true
    )

    @Test
    fun `login success saves token and returns user`() = runTest {
        val api = mock<ApiService>()
        val session = mock<SessionManager>()
        whenever(api.login(any())).thenReturn(
            Response.success(AuthResponse(token = "fake.jwt.token", user = fakeUser))
        )

        val repository = AuthRepository(api, session)
        val result = repository.login("asive@example.com", "password123")

        assertTrue(result is ApiResult.Success)
        assertEquals(fakeUser, (result as ApiResult.Success).data)
        verify(session).saveToken("fake.jwt.token")
    }

    @Test
    fun `login failure returns Failure without saving a token`() = runTest {
        val api = mock<ApiService>()
        val session = mock<SessionManager>()
        val errorBody = """{"error":"Invalid email or password"}"""
            .toResponseBody("application/json".toMediaTypeOrNull())
        whenever(api.login(any())).thenReturn(Response.error(401, errorBody))

        val repository = AuthRepository(api, session)
        val result = repository.login("asive@example.com", "wrongpass")

        assertTrue(result is ApiResult.Failure)
        assertEquals("Invalid email or password", (result as ApiResult.Failure).message)
    }

    @Test
    fun `loginWithGoogle success saves token and returns user`() = runTest {
        val api = mock<ApiService>()
        val session = mock<SessionManager>()
        whenever(api.loginWithGoogle(any())).thenReturn(
            Response.success(AuthResponse(token = "fake.google.jwt.token", user = fakeUser))
        )

        val repository = AuthRepository(api, session)
        val result = repository.loginWithGoogle("dummy_id_token")

        assertTrue(result is ApiResult.Success)
        assertEquals(fakeUser, (result as ApiResult.Success).data)
        verify(session).saveToken("fake.google.jwt.token")
    }
}
