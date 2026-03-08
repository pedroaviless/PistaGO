package me.nacimiento.pistago.data.repository

import me.nacimiento.pistago.data.local.TokenDataStore
import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.data.remote.dto.LoginRequest
import me.nacimiento.pistago.data.remote.dto.RegisterRequest
import me.nacimiento.pistago.domain.model.Usuario
import me.nacimiento.pistago.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: PistaGoApi,
    private val tokenDataStore: TokenDataStore
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

    override suspend fun getToken(): String? {
        return tokenDataStore.token.first()
    }
}