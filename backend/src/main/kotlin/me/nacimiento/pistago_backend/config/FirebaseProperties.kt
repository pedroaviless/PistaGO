package me.nacimiento.pistago_backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pistago.firebase")
data class FirebaseProperties(
    val serviceAccountPath: String = "",
    val enabled: Boolean = false
)