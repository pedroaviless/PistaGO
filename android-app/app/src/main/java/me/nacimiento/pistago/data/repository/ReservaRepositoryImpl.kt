package me.nacimiento.pistago.data.repository

import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.data.remote.dto.ReservaRequest
import me.nacimiento.pistago.domain.model.Reserva
import me.nacimiento.pistago.domain.repository.ReservaRepository
import javax.inject.Inject

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
                Result.failure(Exception("Error: ${response.code()}"))
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
                Result.failure(Exception("Error: ${response.code()}"))
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
                Result.failure(Exception("Error: ${response.code()}"))
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
                Result.failure(Exception("Error: ${response.code()}"))
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
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}