package me.nacimiento.pistago.data.repository


import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.domain.model.DiaSemanaStat
import me.nacimiento.pistago.domain.model.Estadisticas
import me.nacimiento.pistago.domain.model.PistaStat
import me.nacimiento.pistago.domain.model.UsuarioStat
import me.nacimiento.pistago.domain.repository.EstadisticasRepository
import retrofit2.Response
import me.nacimiento.pistago.data.remote.extractErrorMessage
import javax.inject.Inject



class EstadisticasRepositoryImpl @Inject constructor(
    private val api: PistaGoApi
) : EstadisticasRepository {

    override suspend fun getEstadisticas(): Result<Estadisticas> {
        return try {
            val response = api.getEstadisticas()
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    Estadisticas(
                        totalUsuarios = dto.totalUsuarios,
                        totalReservas = dto.totalReservas,
                        reservasConfirmadas = dto.reservasConfirmadas,
                        reservasCanceladas = dto.reservasCanceladas,
                        reservasHoy = dto.reservasHoy,
                        tasaCancelacion = dto.tasaCancelacion,
                        topPistas = dto.topPistas.map { PistaStat(it.nombrePista, it.totalReservas) },
                        topUsuarios = dto.topUsuarios.map { UsuarioStat(it.nombreUsuario, it.totalReservas) },
                        reservasPorDiaSemana = dto.reservasPorDiaSemana.map { DiaSemanaStat(it.dia, it.total) }
                    )
                )
            } else {
                Result.failure(Exception(response.extractErrorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}