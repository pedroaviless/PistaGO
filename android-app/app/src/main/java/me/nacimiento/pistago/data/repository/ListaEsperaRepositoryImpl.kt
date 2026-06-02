package me.nacimiento.pistago.data.repository

import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.data.remote.dto.ListaEsperaRequest
import me.nacimiento.pistago.domain.model.ListaEspera
import me.nacimiento.pistago.domain.repository.ListaEsperaRepository
import javax.inject.Inject
import me.nacimiento.pistago.data.remote.extractErrorMessage

class ListaEsperaRepositoryImpl @Inject constructor(
    private val api: PistaGoApi
) : ListaEsperaRepository {

    override suspend fun apuntarse(pistaId: Long, fechaHora: String): Result<ListaEspera> {
        return try {
            val response = api.apuntarseListaEspera(ListaEsperaRequest(pistaId, fechaHora))
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(ListaEspera(body.id, body.pistaId, body.nombrePista, body.fechaHora, body.posicion, "ACTIVO"))
            } else {
                Result.failure(Exception(response.extractErrorMessage("no se pudo apuntar a la lista de espera")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMiLista(): Result<List<ListaEspera>> {
        return try {
            val response = api.getMiListaEspera()
            if (response.isSuccessful) {
                val lista = response.body()!!.map {
                    ListaEspera(it.id, it.pistaId, it.nombrePista, it.fechaHora, it.posicion, "ACTIVO")
                }
                Result.success(lista)
            } else {
                Result.failure(Exception(response.extractErrorMessage("no se pudo cargar la lista de espera")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun salir(id: Long): Result<Unit> {
        return try {
            val response = api.salirListaEspera(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("no se pudo salir de la lista de espera")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}