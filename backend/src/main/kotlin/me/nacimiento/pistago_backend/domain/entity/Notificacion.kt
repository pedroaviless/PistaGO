package me.nacimiento.pistago_backend.domain.entity

import jakarta.persistence.*
import me.nacimiento.pistago_backend.domain.enums.TipoNotificacion
import java.time.LocalDateTime

@Entity
@Table(name = "notificaciones")
data class Notificacion(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    val usuario: Usuario = Usuario(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipo: TipoNotificacion = TipoNotificacion.CONFIRMACION_RESERVA,

    @Column(nullable = false)
    val titulo: String = "",

    @Column(nullable = false)
    val mensaje: String = "",

    @Column(nullable = false)
    val leida: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    val reserva: Reserva? = null,

    @Column(name = "enviada_push", nullable = false)
    val enviadaPush: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)