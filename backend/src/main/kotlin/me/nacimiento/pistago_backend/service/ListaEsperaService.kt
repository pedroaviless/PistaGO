package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.entity.ListaEspera
import me.nacimiento.pistago_backend.domain.repository.ListaEsperaRepository
import me.nacimiento.pistago_backend.domain.repository.PistaRepository
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.ListaEsperaRequest
import me.nacimiento.pistago_backend.dto.ListaEsperaResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ListaEsperaService(
    private val listaEsperaRepository: ListaEsperaRepository,
    private val pistaRepository: PistaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    @Transactional
    fun apuntarse(email: String, request: ListaEsperaRequest): ListaEsperaResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val pista = pistaRepository.findById(request.pistaId)
            .orElseThrow { IllegalArgumentException("Pista no encontrada") }

        val yaEnLista = listaEsperaRepository.findByUsuarioIdAndPistaIdAndFechaHora(
            usuario.id, request.pistaId, request.fechaHora
        )
        if (yaEnLista != null) throw IllegalArgumentException("Ya estás en la lista de espera para este horario")

        // Posición orientativa al guardar (basada en cuántos hay en ese momento).
        // No es la fuente de verdad: la posición real se calcula al consultar.
        val listaActual = listaEsperaRepository
            .findByPistaIdAndFechaHoraOrderByCreatedAtAsc(request.pistaId, request.fechaHora)
        val posicionInicial = listaActual.size + 1

        val entrada = ListaEspera(
            usuario = usuario,
            pista = pista,
            fechaHora = request.fechaHora,
            posicion = posicionInicial,
            notificado = false,
            createdAt = LocalDateTime.now()
        )

        val guardada = listaEsperaRepository.save(entrada)
        return guardada.toResponseConPosicionCalculada()
    }

    @Transactional(readOnly = true)
    fun getMiLista(email: String): List<ListaEsperaResponse> {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        return listaEsperaRepository.findByUsuarioId(usuario.id)
            .map { it.toResponseConPosicionCalculada() }
    }

    @Transactional
    fun salir(email: String, listaEsperaId: Long) {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
        val entrada = listaEsperaRepository.findById(listaEsperaId)
            .orElseThrow { NoSuchElementException("Entrada no encontrada") }
        if (entrada.usuario.id != usuario.id) throw IllegalArgumentException("No autorizado")

        listaEsperaRepository.delete(entrada)
        // No hace falta reordenar: la posición se calcula al consultar
    }

    /**
     * Calcula la posición real consultando la cola completa de esa pista+fechaHora
     * y buscando el índice de esta entrada (ordenado por createdAt).
     */
    private fun ListaEspera.toResponseConPosicionCalculada(): ListaEsperaResponse {
        val cola = listaEsperaRepository
            .findByPistaIdAndFechaHoraOrderByCreatedAtAsc(pista.id, fechaHora)
        val posicionReal = cola.indexOfFirst { it.id == this.id } + 1

        return ListaEsperaResponse(
            id = id,
            pistaId = pista.id,
            nombrePista = pista.nombre,
            usuarioId = usuario.id,
            nombreUsuario = usuario.nombre,
            fechaHora = fechaHora,
            posicion = posicionReal,
            notificado = notificado
        )
    }
}