package com.skillswap.app.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the JWT issued at login/register using Android's EncryptedSharedPreferences,
 * which encrypts both keys and values at rest using a key held in the Android
 * Keystore. The app NEVER stores the user's plaintext password anywhere on the
 * device - only this signed, expiring token, matching the "no further screen is
 * reachable without a valid session token" requirement from Part 1 section 4.1.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "skillswap_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Falls back to regular prefs only if the Keystore itself is unavailable
        // (e.g. some emulators); logged loudly since this should not happen on
        // a real device.
        Log.e(TAG, "EncryptedSharedPreferences unavailable, falling back", e)
        context.getSharedPreferences("skillswap_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val TAG = "SessionManager"
        private const val KEY_TOKEN = "jwt_token"
    }
}
