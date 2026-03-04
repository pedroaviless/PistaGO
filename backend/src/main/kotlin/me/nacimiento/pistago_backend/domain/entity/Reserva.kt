package me.nacimiento.pistago_backend.domain.entity

import jakarta.persistence.*
import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import java.time.LocalDateTime

@Entity
@Table(
    name = "reservas",
    indexes = [Index(name = "idx_pista_fecha_estado", columnList = "pista_id, fecha_hora, estado")]
)
data class Reserva(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    val usuario: Usuario = Usuario(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pista_id", nullable = false)
    val pista: Pista = Pista(),

    @Column(name = "fecha_hora", nullable = false)
    val fechaHora: LocalDateTime = LocalDateTime.now(),

    @Column(name = "duracion_min", nullable = false)
    val duracionMin: Int = 90,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val estado: EstadoReserva = EstadoReserva.CONFIRMADA,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelada_por")
    val canceladaPor: Usuario? = null,

    @Column(name = "motivo_cancel")
    val motivoCancel: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)