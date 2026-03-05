package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.entity.Pista
import me.nacimiento.pistago_backend.domain.repository.PistaRepository
import me.nacimiento.pistago_backend.dto.PistaRequest
import me.nacimiento.pistago_backend.dto.PistaResponse
import org.springframework.stereotype.Service

@Service
class PistaService(
    private val pistaRepository: PistaRepository
) {

    fun getAll(): List<PistaResponse> =
        pistaRepository.findAll().map { it.toResponse() }

    fun getActivas(): List<PistaResponse> =
        pistaRepository.findByActiva(true).map { it.toResponse() }

    fun getById(id: Long): PistaResponse =
        pistaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Pista no encontrada") }
            .toResponse()

    fun create(request: PistaRequest): PistaResponse {
        val pista = Pista(
            nombre = request.nombre,
            tipo = request.tipo,
            descripcion = request.descripcion,
            activa = true
        )
        return pistaRepository.save(pista).toResponse()
    }

    fun update(id: Long, request: PistaRequest): PistaResponse {
        val pista = pistaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Pista no encontrada") }
        val updated = pista.copy(
            nombre = request.nombre,
            tipo = request.tipo,
            descripcion = request.descripcion
        )
        return pistaRepository.save(updated).toResponse()
    }

    fun setActiva(id: Long, activa: Boolean): PistaResponse {
        val pista = pistaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Pista no encontrada") }
        return pistaRepository.save(pista.copy(activa = activa)).toResponse()
    }

    private fun Pista.toResponse() = PistaResponse(
        id = id!!,
        nombre = nombre,
        tipo = tipo,
        descripcion = descripcion,
        activa = activa
    )
}