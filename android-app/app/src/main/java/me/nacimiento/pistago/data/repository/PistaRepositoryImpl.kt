package me.nacimiento.pistago.data.repository

import me.nacimiento.pistago.data.remote.api.PistaGoApi
import me.nacimiento.pistago.data.remote.dto.PistaAdminRequest
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
                Result.success(response.body()!!.map { Pista(it.id, it.nombre, it.tipo, it.descripcion, it.activa) })
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

    override suspend fun getTodasLasPistas(): Result<List<Pista>> {
        return try {
            val response = api.todasLasPistas()
            if (response.isSuccessful) {
                Result.success(response.body()!!.map { Pista(it.id, it.nombre, it.tipo, it.descripcion, it.activa) })
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearPista(nombre: String, tipo: String, descripcion: String?): Result<Pista> {
        return try {
            val response = api.crearPista(PistaAdminRequest(nombre, tipo, descripcion))
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

    override suspend fun actualizarPista(id: Long, nombre: String, tipo: String, descripcion: String?): Result<Pista> {
        return try {
            val response = api.actualizarPista(id, PistaAdminRequest(nombre, tipo, descripcion))
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

    override suspend fun setActivaPista(id: Long, activa: Boolean): Result<Pista> {
        return try {
            val response = api.setActivaPista(id, activa)
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