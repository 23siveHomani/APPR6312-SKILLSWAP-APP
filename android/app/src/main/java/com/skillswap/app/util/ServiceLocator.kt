package com.skillswap.app.util

import android.content.Context
import com.skillswap.app.data.api.ApiService
import com.skillswap.app.data.api.RetrofitClient
import com.skillswap.app.data.local.AppDatabase
import com.skillswap.app.data.repository.AuthRepository
import com.skillswap.app.data.repository.ListingRepository

/**
 * Deliberately simple hand-rolled service locator instead of a DI framework
 * (Hilt/Koin) - keeps the moving parts easy to explain in the README and in
 * the demo video.
 */
object ServiceLocator {

    @Volatile private var sessionManager: SessionManager? = null
    @Volatile private var apiService: ApiService? = null

    fun sessionManager(context: Context): SessionManager =
        sessionManager ?: synchronized(this) {
            sessionManager ?: SessionManager(context.applicationContext).also { sessionManager = it }
        }

    fun apiService(context: Context): ApiService =
        apiService ?: synchronized(this) {
            apiService ?: RetrofitClient.create(sessionManager(context)).also { apiService = it }
        }

    fun authRepository(context: Context): AuthRepository =
        AuthRepository(apiService(context), sessionManager(context))

    fun listingRepository(context: Context): ListingRepository {
        val db = AppDatabase.getInstance(context)
        return ListingRepository(
            apiService(context),
            db.listingDao(),
            db.pendingSyncDao(),
            context.applicationContext
        )
    }
}
