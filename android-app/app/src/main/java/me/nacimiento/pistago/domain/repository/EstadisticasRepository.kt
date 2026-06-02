package me.nacimiento.pistago.domain.repository

import me.nacimiento.pistago.domain.model.Estadisticas

interface EstadisticasRepository {
    suspend fun getEstadisticas(): Result<Estadisticas>
}