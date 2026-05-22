package me.nacimiento.pistago_backend.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import me.nacimiento.pistago_backend.config.FirebaseProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FcmService(
    private val firebaseProperties: FirebaseProperties
) {
    private val log = LoggerFactory.getLogger(FcmService::class.java)

    /**
     * Envía una notificación push a un dispositivo concreto.
     * Devuelve true si se envió correctamente, false si falló o Firebase está deshabilitado.
     */
    fun enviarNotificacion(
        fcmToken: String?,
        titulo: String,
        cuerpo: String,
        datos: Map<String, String> = emptyMap()
    ): Boolean {
        if (!firebaseProperties.enabled) {
            log.warn("Firebase deshabilitado, no se envía push.")
            return false
        }
        if (fcmToken.isNullOrBlank()) {
            log.warn("El usuario no tiene fcm_token, no se puede enviar push.")
            return false
        }

        return try {
            val message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(titulo)
                        .setBody(cuerpo)
                        .build()
                )
                .putAllData(datos)
                .build()

            val response = FirebaseMessaging.getInstance().send(message)
            log.info("Push enviada correctamente: $response")
            true
        } catch (e: Exception) {
            log.error("Error enviando push: ${e.message}", e)
            false
        }
    }
}