package me.nacimiento.pistago_backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.FileInputStream

@Component
class FirebaseConfig(
    private val firebaseProperties: FirebaseProperties
) {
    private val log = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun init() {
        if (!firebaseProperties.enabled) {
            log.warn("Firebase está deshabilitado (pistago.firebase.enabled=false). No se enviarán notificaciones push.")
            return
        }

        if (FirebaseApp.getApps().isNotEmpty()) {
            log.info("Firebase ya estaba inicializado.")
            return
        }

        try {
            val serviceAccount = FileInputStream(firebaseProperties.serviceAccountPath)
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
            log.info("Firebase inicializado correctamente.")
        } catch (e: Exception) {
            log.error("Error inicializando Firebase: ${e.message}", e)
        }
    }
}