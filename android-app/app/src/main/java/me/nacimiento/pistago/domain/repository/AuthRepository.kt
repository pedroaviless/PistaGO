package me.nacimiento.pistago.domain.repository

import me.nacimiento.pistago.domain.model.Usuario

interface AuthRepository {
    suspend fun register(nombre: String, email: String, password: String): Result<Usuario>
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun logout()
    suspend fun getToken(): String?
    suspend fun getUsuarioActual(): Result<Usuario?>
}