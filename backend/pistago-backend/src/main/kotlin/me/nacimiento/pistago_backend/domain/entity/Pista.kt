package me.nacimiento.pistago_backend.domain.entity


import jakarta.persistence.*
import me.nacimiento.pistago_backend.domain.enums.TipoPista

@Entity
@Table(name = "pistas")
data class Pista(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val nombre: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipo: TipoPista = TipoPista.TIERRA_BATIDA,

    @Column(nullable = false)
    val activa: Boolean = true,

    val descripcion: String? = null
)