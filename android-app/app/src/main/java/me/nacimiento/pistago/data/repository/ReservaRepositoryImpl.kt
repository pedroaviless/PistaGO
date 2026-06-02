package me.nacimiento.pistago.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.data.remote.dto.ReservaRequest
import me.nacimiento.pistago.domain.model.Reserva
import me.nacimiento.pistago.domain.repository.ReservaRepository
import retrofit2.Response
import javax.inject.Inject

/**
 * DTO interno para parsear el cuerpo de error que devuelve el backend
 * (ver GlobalExceptionHandler en el backend).
 */
private data class ApiErrorBody(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)

/**
 * Extrae un mensaje legible del errorBody de Retrofit.
 * Si el cuerpo es un JSON con campo "message" (formato GlobalExceptionHandler),
 * lo devuelve. Si no, devuelve un mensaje genérico con el código HTTP.
 */
private fun <T> Response<T>.extractErrorMessage(): String {
    val raw = errorBody()?.string().orEmpty()
    if (raw.isNotBlank()) {
        try {
            val parsed = Gson().fromJson(raw, ApiErrorBody::class.java)
            val msg = parsed?.message
            if (!msg.isNullOrBlank()) return msg
        } catch (_: JsonSyntaxException) {
            // El cuerpo no era JSON con el formato esperado; caemos al fallback.
        }
    }
    return "Error ${code()}: no se pudo completar la operación"
}

class ReservaRepositoryImpl @Inject constructor(
    private val api: PistaGoApi
) : ReservaRepository {

    override suspend fun crearReserva(pistaId: Long, fechaHora: String, duracionMin: Int): Result<Reserva> {
        return try {
            val response = api.crearReserva(ReservaRequest(pistaId, fechaHora, duracionMin))
            if (response.isSuccessful) {
                val r = response.body()!!
                Result.success(Reserva(r.id, r.pistaId, r.nombrePista, r.usuarioId, r.nombreUsuario, r.fechaHora, r.duracionMin, r.estado))
            } else {
                Result.failure(Exception(response.extractErrorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMisReservas(): Result<List<Reserva>> {
        return try {
            val response = api.getMisReservas()
            if (response.isSuccessful) {
                val reservas = response.body()!!.map {
                    Reserva(it.id, it.pistaId, it.nombrePista, it.usuarioId, it.nombreUsuario, it.fechaHora, it.duracionMin, it.estado)
                }
                Result.success(reservas)
            } else {
                Result.failure(Exception(response.extractErrorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelarReserva(id: Long): Result<Reserva> {
        return try {
            val response = api.cancelarReserva(id)
            if (response.isSuccessful) {
                val r = response.body()!!
                Result.success(Reserva(r.id, r.pistaId, r.nombrePista, r.usuarioId, r.nombreUsuario, r.fechaHora, r.duracionMin, r.estado))
            } else {
                Result.failure(Exception(response.extractErrorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodasLasReservas(): Result<List<Reserva>> {
        return try {
            val response = api.getTodasLasReservas()
            if (response.isSuccessful) {
                val reservas = response.body()!!.map {
                    Reserva(it.id, it.pistaId, it.nombrePista, it.usuarioId, it.nombreUsuario, it.fechaHora, it.duracionMin, it.estado)
                }
                Result.success(reservas)
            } else {
                Result.failure(Exception(response.extractErrorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDisponibilidad(fecha: String, pistaId: Long): Result<List<String>> {
        return try {
            val response = api.getDisponibilidad(fecha, pistaId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.extractErrorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}