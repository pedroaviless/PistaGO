package me.nacimiento.pistago.domain.repository

import me.nacimiento.pistago.domain.model.Pista

interface PistaRepository {
    suspend fun getPistas(): Result<List<Pista>>
    suspend fun getPistaById(id: Long): Result<Pista>
}