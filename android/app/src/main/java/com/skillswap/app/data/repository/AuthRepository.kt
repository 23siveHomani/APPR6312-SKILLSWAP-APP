package com.skillswap.app.data.repository

import com.skillswap.app.data.api.ApiService
import com.skillswap.app.data.model.*
import com.skillswap.app.util.SessionManager

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val message: String) : ApiResult<Nothing>()
}

/**
 * Wraps register/login/settings calls. Kept as a plain class (constructor
 * injection, no singletons) specifically so it's easy to construct with a
 * fake ApiService in unit tests - see LoginViewModelTest.
 */
class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun register(name: String, email: String, password: String): ApiResult<User> {
        return try {
            val response = api.register(RegisterRequest(name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveToken(body.token)
                ApiResult.Success(body.user)
            } else {
                ApiResult.Failure(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Network error")
        }
    }

    suspend fun login(email: String, password: String): ApiResult<User> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveToken(body.token)
                ApiResult.Success(body.user)
            } else {
                ApiResult.Failure(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Network error")
        }
    }

    suspend fun loginWithGoogle(idToken: String, email: String? = null, name: String? = null): ApiResult<User> {
        return try {
            val response = api.loginWithGoogle(GoogleAuthRequest(idToken = idToken, email = email, name = name))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveToken(body.token)
                ApiResult.Success(body.user)
            } else {
                ApiResult.Failure(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Network error")
        }
    }

    suspend fun updateSettings(update: SettingsUpdateRequest): ApiResult<User> {
        return try {
            val response = api.updateSettings(update)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.user)
            } else {
                ApiResult.Failure(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Network error")
        }
    }

    fun logout() = sessionManager.clear()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    private fun parseError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Something went wrong"
        return try {
            com.google.gson.Gson().fromJson(raw, ApiError::class.java).error
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}
