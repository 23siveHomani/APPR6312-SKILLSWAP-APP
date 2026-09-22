package com.skillswap.app.data.model

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val languagePref: String,
    val timeCredits: Int,
    val notificationsEnabled: Boolean,
    val syncOnWifiOnly: Boolean
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SettingsUpdateRequest(
    val languagePref: String? = null,
    val notificationsEnabled: Boolean? = null,
    val syncOnWifiOnly: Boolean? = null,
    val fcmToken: String? = null
)

data class SkillListing(
    val _id: String,
    val title: String,
    val category: String,
    val description: String?,
    val ownerId: OwnerSummary?
)

data class OwnerSummary(
    val _id: String,
    val name: String,
    val timeCredits: Int
)

data class ListingsResponse(val listings: List<SkillListing>)
data class UserResponse(val user: User)

data class ApiError(val error: String)
