package com.skillswap.app

import com.skillswap.app.util.PasswordUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PasswordUtilsTest {

    @Test
    fun `hashPassword returns consistent sha256 hash`() {
        val hash1 = PasswordUtils.hashPassword("Password123")
        val hash2 = PasswordUtils.hashPassword("Password123")
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length)
    }

    @Test
    fun `hashPassword produces different hashes for different passwords`() {
        val hash1 = PasswordUtils.hashPassword("Password123")
        val hash2 = PasswordUtils.hashPassword("Password124")
        assertNotEquals(hash1, hash2)
    }
}
