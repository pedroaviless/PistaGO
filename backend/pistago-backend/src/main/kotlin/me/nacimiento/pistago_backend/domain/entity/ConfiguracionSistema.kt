package me.nacimiento.pistago_backend.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "configuracion_sistema")
data class ConfiguracionSistema(

    @Id
    val id: Long = 1,

    @Column(name = "max_reservas_semana", nullable = false)
    val maxReservasSemana: Int = 3,

    @Column(name = "horas_cancelacion_minima", nullable = false)
    val horasCancelacionMinima: Int = 24,

    @Column(name = "minutos_confirmacion_espera", nullable = false)
    val minutosConfirmacionEspera: Int = 10,

    @Column(name = "duracion_franja_min", nullable = false)
    val duracionFranjaMin: Int = 90,

    @Column(name = "hora_apertura", nullable = false)
    val horaApertura: LocalTime = LocalTime.of(8, 0),

    @Column(name = "hora_cierre", nullable = false)
    val horaCierre: LocalTime = LocalTime.of(22, 0),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    val updatedBy: Usuario? = null,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)