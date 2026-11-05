package com.skillswap.app.util

import java.security.MessageDigest

/**
 * Utility for password security. Passwords are sent over HTTPS to the backend,
 * where bcrypt hashes them before saving to MongoDB / NoSQL database.
 * EncryptedSharedPreferences (AES-256 GCM) is used to store session tokens securely
 * without storing raw passwords locally.
 */
object PasswordUtils {

    /**
     * Computes SHA-256 hash of password string for client-side hashing if required.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
