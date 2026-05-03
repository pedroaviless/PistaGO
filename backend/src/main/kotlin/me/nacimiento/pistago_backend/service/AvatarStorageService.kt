package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.config.UploadsProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class AvatarStorageService(
    private val uploadsProperties: UploadsProperties
) {

    private val allowedTypes = setOf("image/jpeg", "image/png", "image/webp")

    private val avatarsPath: Path by lazy {
        Paths.get(uploadsProperties.basePath, uploadsProperties.avatarsDir).also {
            Files.createDirectories(it)
        }
    }

    fun guardar(file: MultipartFile, usuarioId: Long): String {
        require(!file.isEmpty) { "El archivo está vacío" }
        require(file.contentType in allowedTypes) {
            "Tipo de archivo no permitido. Usa JPG, PNG o WEBP"
        }

        val extension = when (file.contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Tipo no soportado")
        }

        val filename = "user_${usuarioId}_${UUID.randomUUID()}.$extension"
        val destino = avatarsPath.resolve(filename)

        file.inputStream.use { input ->
            Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING)
        }

        return "${uploadsProperties.publicUrl}/${uploadsProperties.avatarsDir}/$filename"
    }

    fun borrar(fotoUrl: String?) {
        if (fotoUrl.isNullOrBlank()) return
        val filename = fotoUrl.substringAfterLast("/")
        val path = avatarsPath.resolve(filename)
        runCatching { Files.deleteIfExists(path) }
    }
}