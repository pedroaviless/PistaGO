package me.nacimiento.pistago.data.remote

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response

/**
 * Estructura del cuerpo de error que devuelve el backend de PistaGO.
 * Coincide con el formato de ErrorResponse del GlobalExceptionHandler.
 */
data class ApiErrorBody(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val errors: Map<String, String>? = null
)

/**
 * Extrae un mensaje legible del errorBody de Retrofit.
 *
 * Estrategia:
 *   1. Si el body es un JSON con campo "message" (formato GlobalExceptionHandler),
 *      lo devuelve directamente.
 *   2. Si tiene errores de validación (Map "errors"), devuelve el primero.
 *   3. Si no, devuelve un fallback genérico con el código HTTP.
 *
 * @param fallback texto base usado cuando no se puede parsear el body.
 */
fun <T> Response<T>.extractErrorMessage(
    fallback: String = "no se pudo completar la operación"
): String {
    val raw = errorBody()?.string().orEmpty()
    if (raw.isNotBlank()) {
        try {
            val parsed = Gson().fromJson(raw, ApiErrorBody::class.java)
            val msg = parsed?.message
            if (!msg.isNullOrBlank()) return msg
            // Si hay errores de validación, devolvemos el primero
            val firstFieldError = parsed?.errors?.entries?.firstOrNull()
            if (firstFieldError != null) {
                return "${firstFieldError.key}: ${firstFieldError.value}"
            }
        } catch (_: JsonSyntaxException) {
            // El body no era JSON, caemos al fallback
        }
    }
    return "Error ${code()}: $fallback"
}