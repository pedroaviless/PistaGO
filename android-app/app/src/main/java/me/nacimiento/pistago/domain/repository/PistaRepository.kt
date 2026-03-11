package me.nacimiento.pistago.domain.repository

import me.nacimiento.pistago.domain.model.Pista

interface PistaRepository {
    suspend fun getPistas(): Result<List<Pista>>
    suspend fun getPistaById(id: Long): Result<Pista>
    suspend fun getTodasLasPistas(): Result<List<Pista>>
    suspend fun crearPista(nombre: String, tipo: String, descripcion: String?): Result<Pista>
    suspend fun actualizarPista(id: Long, nombre: String, tipo: String, descripcion: String?): Result<Pista>
    suspend fun setActivaPista(id: Long, activa: Boolean): Result<Pista>
}