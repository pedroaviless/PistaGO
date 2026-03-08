package me.nacimiento.pistago.data.repository

import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.domain.model.Pista
import me.nacimiento.pistago.domain.repository.PistaRepository
import javax.inject.Inject

class PistaRepositoryImpl @Inject constructor(
    private val api: PistaGoApi
) : PistaRepository {

    override suspend fun getPistas(): Result<List<Pista>> {
        return try {
            val response = api.getPistas()
            if (response.isSuccessful) {
                val pistas = response.body()!!.map {
                    Pista(it.id, it.nombre, it.tipo, it.descripcion, it.activa)
                }
                Result.success(pistas)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPistaById(id: Long): Result<Pista> {
        return try {
            val response = api.getPistaById(id)
            if (response.isSuccessful) {
                val p = response.body()!!
                Result.success(Pista(p.id, p.nombre, p.tipo, p.descripcion, p.activa))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}