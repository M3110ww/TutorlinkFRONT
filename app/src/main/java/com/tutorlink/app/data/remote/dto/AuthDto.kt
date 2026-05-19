package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

enum class UserRole {
    STUDENT, TUTOR, ADMIN
}

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name")     val name: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role")     val role: UserRole
)

data class AuthResponse(
    @SerializedName("token")    val token: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("role")     val role: UserRole,
    @SerializedName("userId")   val userId: Long
)
