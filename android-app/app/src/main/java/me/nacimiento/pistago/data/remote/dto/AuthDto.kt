package me.nacimiento.pistago.data.remote.dto

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val email: String,
    val nombre: String,
    val rol: String
)