package me.nacimiento.pistago_backend.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "lista_espera",
    uniqueConstraints = [UniqueConstraint(columnNames = ["usuario_id", "pista_id", "fecha_hora"])]
)
data class ListaEspera(

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

    @Column(nullable = false)
    val posicion: Int = 0,

    @Column(nullable = false)
    val notificado: Boolean = false,

    @Column(name = "expira_en")
    val expiraEn: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)