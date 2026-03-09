package me.nacimiento.pistago.domain.model

data class Usuario(
    val token: String,
    val email: String,
    val nombre: String,
    val rol: String,
    var fcmToken: String? = null
)