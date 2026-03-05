package me.nacimiento.pistago_backend.dto

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)