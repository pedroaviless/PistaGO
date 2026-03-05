package me.nacimiento.pistago_backend.dto

data class LoginRequest(
    val email: String,
    val password: String
)