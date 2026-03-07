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
            usuario.id!!, request.pistaId, request.fechaHora
        )
        if (yaEnLista != null) throw IllegalArgumentException("Ya estás en la lista de espera para este horario")

        val listaActual = listaEsperaRepository
            .findByPistaIdAndFechaHoraOrderByPosicionAsc(request.pistaId, request.fechaHora)
        val posicion = listaActual.size + 1

        val entrada = ListaEspera(
            usuario = usuario,
            pista = pista,
            fechaHora = request.fechaHora,
            posicion = posicion,
            notificado = false,
            createdAt = LocalDateTime.now()
        )

        return listaEsperaRepository.save(entrada).toResponse()
    }

    @Transactional
    fun getMiLista(email: String): List<ListaEsperaResponse> {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
        return listaEsperaRepository.findByUsuarioId(usuario.id!!)
            .map { it.toResponse() }
    }

    @Transactional
    fun salir(email: String, listaEsperaId: Long) {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
        val entrada = listaEsperaRepository.findById(listaEsperaId)
            .orElseThrow { NoSuchElementException("Entrada no encontrada") }
        if (entrada.usuario.id != usuario.id) throw IllegalArgumentException("No autorizado")
        listaEsperaRepository.delete(entrada)
    }

    private fun ListaEspera.toResponse() = ListaEsperaResponse(
        id = id!!,
        pistaId = pista.id!!,
        nombrePista = pista.nombre,
        usuarioId = usuario.id!!,
        nombreUsuario = usuario.nombre,
        fechaHora = fechaHora,
        posicion = posicion,
        notificado = notificado
    )
}