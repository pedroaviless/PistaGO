package me.nacimiento.pistago_backend.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "bloqueos_horario")
data class BloqueoHorario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pista_id", nullable = false)
    val pista: Pista = Pista(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    val admin: Usuario = Usuario(),

    @Column(name = "fecha_hora", nullable = false)
    val fechaHora: LocalDateTime = LocalDateTime.now(),

    @Column(name = "duracion_min", nullable = false)
    val duracionMin: Int = 90,

    @Column(nullable = false)
    val motivo: String = ""
)