package me.nacimiento.pistago.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import me.nacimiento.pistago.data.local.TokenDataStore
import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.data.remote.dto.LoginRequest
import me.nacimiento.pistago.data.remote.dto.PasswordChangeRequest
import me.nacimiento.pistago.data.remote.dto.PerfilRequest
import me.nacimiento.pistago.data.remote.dto.PerfilResponse
import me.nacimiento.pistago.data.remote.dto.RegisterRequest
import me.nacimiento.pistago.domain.model.Usuario
import me.nacimiento.pistago.domain.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: PistaGoApi,
    private val tokenDataStore: TokenDataStore,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun register(nombre: String, email: String, password: String): Result<Usuario> {
        return try {
            val response = api.register(RegisterRequest(nombre, email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenDataStore.saveSession(body.token, body.email, body.nombre, body.rol)
                Result.success(Usuario(body.token, body.email, body.nombre, body.rol))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenDataStore.saveSession(body.token, body.email, body.nombre, body.rol)
                Result.success(Usuario(body.token, body.email, body.nombre, body.rol))
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenDataStore.clearSession()
    }

    override suspend fun getToken(): String? = tokenDataStore.token.first()

    override suspend fun getUsuarioActual(): Result<Usuario?> {
        return try {
            val token = tokenDataStore.token.first()
            val email = tokenDataStore.email.first()
            val nombre = tokenDataStore.nombre.first()
            val rol = tokenDataStore.rol.first()
            if (token != null && email != null && nombre != null && rol != null) {
                Result.success(Usuario(token, email, nombre, rol))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPerfil(): Result<PerfilResponse> {
        return try {
            val response = api.getPerfil()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarPerfil(
        nombre: String,
        telefono: String?
    ): Result<PerfilResponse> {
        return try {
            val response = api.actualizarPerfil(PerfilRequest(nombre, telefono))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al actualizar perfil: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cambiarPassword(
        passwordActual: String,
        passwordNueva: String
    ): Result<Unit> {
        return try {
            val response = api.cambiarPassword(PasswordChangeRequest(passwordActual, passwordNueva))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Contraseña actual incorrecta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subirFotoPerfil(uri: Uri): Result<PerfilResponse> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("No se pudo abrir la imagen"))

            val bytes = inputStream.use { it.readBytes() }

            // Detectar tipo MIME (con fallback a jpeg)
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "avatar.$extension", requestBody)

            val response = api.subirFotoPerfil(part)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al subir foto: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}