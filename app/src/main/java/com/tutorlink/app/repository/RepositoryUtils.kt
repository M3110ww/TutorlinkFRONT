package com.tutorlink.app.repository

import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * Utility to execute a Retrofit call and return a Flow of Resource.
 */
inline fun <reified T> flowSafeCall(crossinline call: suspend () -> Response<T>): Flow<Resource<T>> = flow {
    emit(Resource.Loading())
    try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                emit(Resource.Success(body))
            } else if (response.code() == 204 || T::class == Unit::class) {
                @Suppress("UNCHECKED_CAST")
                emit(Resource.Success(Unit as T))
            } else {
                emit(Resource.Error("Respuesta vacía del servidor"))
            }
        } else {
            emit(Resource.Error("Error ${response.code()}: ${response.message()}"))
        }
    } catch (e: Exception) {
        emit(Resource.Error(e.localizedMessage ?: "Error de conexión"))
    }
}

/**
 * Utility for non-flow suspend calls.
 */
suspend fun <T> safeCall(call: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(response.message())
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Error desconocido")
    }
}
