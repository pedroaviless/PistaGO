package me.nacimiento.pistago.domain.repository

import me.nacimiento.pistago.domain.model.Reserva

interface ReservaRepository {
    suspend fun crearReserva(pistaId: Long, fechaHora: String, duracionMin: Int): Result<Reserva>
    suspend fun getMisReservas(): Result<List<Reserva>>
    suspend fun cancelarReserva(id: Long): Result<Reserva>
    suspend fun getTodasLasReservas(): Result<List<Reserva>>
}