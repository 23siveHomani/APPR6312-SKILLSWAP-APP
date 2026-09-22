package com.skillswap.app.data.api

import android.util.Log
import com.skillswap.app.BuildConfig
import com.skillswap.app.util.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds a single shared Retrofit instance. All requests go over HTTPS in
 * production (BuildConfig.API_BASE_URL should point at the Render deployment);
 * the auth interceptor attaches "Authorization: Bearer <token>" automatically
 * whenever a session token is present, so individual API calls never need to
 * handle that header themselves.
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    fun create(sessionManager: SessionManager): ApiService {
        val authInterceptor = Interceptor { chain ->
            val token = sessionManager.getToken()
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            Log.d(TAG, "-> ${request.method} ${request.url}")
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor { message -> Log.d(TAG, message) }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
