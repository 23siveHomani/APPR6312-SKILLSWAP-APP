package com.skillswap.app

import com.skillswap.app.ui.login.LoginViewModel
import com.skillswap.app.ui.register.RegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * These tests exercise the pure validation logic in LoginViewModel and
 * RegisterViewModel directly (no Android framework classes involved), so
 * they run fast as plain JUnit tests via `./gradlew test` and in CI.
 */
class ValidationTest {

    private val loginViewModel = LoginViewModel(mock())
    private val registerViewModel = RegisterViewModel(mock())

    @Test
    fun `login validate rejects blank email`() {
        val error = loginViewModel.validate("", "password123")
        assertEquals("Enter a valid email address", error)
    }

    @Test
    fun `login validate rejects malformed email`() {
        val error = loginViewModel.validate("not-an-email", "password123")
        assertEquals("Enter a valid email address", error)
    }

    @Test
    fun `login validate rejects short password`() {
        val error = loginViewModel.validate("user@example.com", "123")
        assertEquals("Password must be at least 6 characters", error)
    }

    @Test
    fun `login validate accepts valid input`() {
        val error = loginViewModel.validate("user@example.com", "password123")
        assertNull(error)
    }

    @Test
    fun `register validate rejects blank name`() {
        val error = registerViewModel.validate("", "user@example.com", "password123", "password123")
        assertEquals("Enter your name", error)
    }

    @Test
    fun `register validate rejects short password`() {
        val error = registerViewModel.validate("Asive", "user@example.com", "short1", "short1")
        assertEquals("Password must be at least 8 characters", error)
    }

    @Test
    fun `register validate rejects mismatched passwords`() {
        val error = registerViewModel.validate(
            "Asive", "user@example.com", "password123", "password124"
        )
        assertEquals("Passwords do not match", error)
    }

    @Test
    fun `register validate accepts valid input`() {
        val error = registerViewModel.validate(
            "Asive", "user@example.com", "password123", "password123"
        )
        assertNull(error)
    }
}
