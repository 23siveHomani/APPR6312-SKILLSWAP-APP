package com.skillswap.app.data.api

import com.skillswap.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Maps directly onto the endpoint list from Part 1 section 5 ("API Design").
 * Every call except register/login/google requires the Authorization header,
 * which is attached automatically by an OkHttp interceptor (see RetrofitClient).
 */
interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponse>

    @PATCH("users/me")
    suspend fun updateSettings(@Body body: SettingsUpdateRequest): Response<UserResponse>

    @GET("listings")
    suspend fun getListings(@Query("category") category: String? = null): Response<ListingsResponse>
}
