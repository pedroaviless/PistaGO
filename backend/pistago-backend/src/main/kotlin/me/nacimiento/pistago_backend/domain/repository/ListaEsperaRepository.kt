package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.ListaEspera
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ListaEsperaRepository : JpaRepository<ListaEspera, Long> {
    fun findByPistaIdAndFechaHoraOrderByPosicionAsc(
        pistaId: Long,
        fechaHora: LocalDateTime
    ): List<ListaEspera>

    fun findByUsuarioIdAndPistaIdAndFechaHora(
        usuarioId: Long,
        pistaId: Long,
        fechaHora: LocalDateTime
    ): ListaEspera?

    fun findByUsuarioId(usuarioId: Long): List<ListaEspera>
}