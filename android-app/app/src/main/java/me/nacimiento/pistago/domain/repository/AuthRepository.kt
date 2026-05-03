package me.nacimiento.pistago.domain.repository

import android.net.Uri
import me.nacimiento.pistago.data.remote.dto.PerfilResponse
import me.nacimiento.pistago.domain.model.Usuario

interface AuthRepository {
    suspend fun register(nombre: String, email: String, password: String): Result<Usuario>
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun logout()
    suspend fun getToken(): String?
    suspend fun getUsuarioActual(): Result<Usuario?>
    suspend fun getPerfil(): Result<PerfilResponse>
    suspend fun actualizarPerfil(nombre: String, telefono: String?): Result<PerfilResponse>
    suspend fun cambiarPassword(passwordActual: String, passwordNueva: String): Result<Unit>
    suspend fun subirFotoPerfil(uri: Uri): Result<PerfilResponse>
}