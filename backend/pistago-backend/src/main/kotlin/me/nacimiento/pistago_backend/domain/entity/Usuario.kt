package me.nacimiento.pistago_backend.domain.entity

import jakarta.persistence.*
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import java.time.LocalDateTime

@Entity
@Table(name = "usuarios")
data class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val nombre: String = "",

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val rol: RolUsuario = RolUsuario.USUARIO,

    @Column(nullable = false)
    val activo: Boolean = true,

    @Column(name = "fcm_token")
    val fcmToken: String? = null,

    @Column(name = "reservas_semana", nullable = false)
    val reservasSemana: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)