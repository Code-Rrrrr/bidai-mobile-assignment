
package com.bid.ai.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val mobile_number: String?,
    val password: String
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val mobile_number: String? = null
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)

data class SignupResponse(
    val message: String,
    val user: User
)

data class ApiError(
    val message: String
)
