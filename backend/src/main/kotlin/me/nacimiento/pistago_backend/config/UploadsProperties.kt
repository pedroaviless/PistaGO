package me.nacimiento.pistago_backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pistago.uploads")
data class UploadsProperties(
    val basePath: String,
    val avatarsDir: String,
    val publicUrl: String
)