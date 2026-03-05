package me.nacimiento.pistago_backend.dto

data class AuthResponse(
    val token: String,
    val email: String,
    val nombre: String,
    val rol: String
)