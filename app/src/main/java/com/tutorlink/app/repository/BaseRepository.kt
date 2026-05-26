package com.tutorlink.app.repository

import com.tutorlink.app.utils.Resource
import org.json.JSONObject
import retrofit2.Response

abstract class BaseRepository {
    protected suspend fun <T> safeCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respuesta vacía del servidor")
                }
            } else {
                val errorRaw = response.errorBody()?.string() ?: ""
                val errorMsg = try {
                    // Intentamos parsear el error si viene en formato JSON
                    val json = JSONObject(errorRaw)
                    val message = json.optString("message", "")
                    
                    // Si el mensaje es el error genérico del servidor, lo cambiamos por algo más útil
                    if (message.isNotBlank() && 
                        !message.contains("Internal Server Error", ignoreCase = true) &&
                        !message.contains("ERROR INTERNO DEL SERVIDOR", ignoreCase = true)) {
                        message
                    } else {
                        getDefaultErrorMessage(response.code())
                    }
                } catch (e: Exception) {
                    getDefaultErrorMessage(response.code())
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: Verifica tu internet")
        }
    }

    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            401 -> "Contraseña o correo incorrectos"
            403 -> "No tienes permiso para realizar esta acción"
            404 -> "El recurso solicitado no se encuentra"
            500 -> "Contraseña o correo incorrectos" // El backend devuelve 500 cuando no encuentra el usuario
            else -> "Ocurrió un error inesperado ($code)"
        }
    }
}
