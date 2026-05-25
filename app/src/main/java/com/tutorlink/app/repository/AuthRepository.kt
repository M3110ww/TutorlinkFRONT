package com.tutorlink.app.repository

import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.*
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: TutoApiService,
    private val sessionManager: SessionManager
) : BaseRepository() {
    val userRole:  Flow<String?> = sessionManager.userRole
    val userName:  Flow<String?> = sessionManager.userName
    val authToken: Flow<String?> = sessionManager.authToken
    val userId:    Flow<Long?>   = sessionManager.userId
    val profileId: Flow<Long?>   = sessionManager.profileId

    fun login(request: LoginRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        val result = safeCall { api.login(request) }
        if (result is Resource.Success) {
            val body = result.data!!
            
            // 1. Guardar sesión
            sessionManager.saveSession(
                token  = body.token,
                role   = body.role.name,
                name   = body.userName,
                userId = body.userId,
                email  = request.email
            )
            
            // 2. Intentar recuperar el profileId inmediatamente
            // Pasamos el token manual porque el interceptor podría fallar en el primer milisegundo
            val bearerToken = "Bearer ${body.token}"
            fetchAndSaveProfileId(body.userId, body.role, bearerToken)
        }
        emit(result)
    }

    fun register(request: RegisterRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        val result = safeCall { api.register(request) }
        
        if (result is Resource.Success) {
            val body = result.data!!
            
            // 1. Guardar la sesión básica (token y userId) para que el interceptor pueda funcionar
            sessionManager.saveSession(
                token  = body.token,
                role   = body.role.name,
                name   = body.userName,
                userId = body.userId,
                email  = request.email
            )
            
            // 2. CREACIÓN OBLIGATORIA DEL PERFIL
            // Pasamos el token manualmente como "plan B" por si el DataStore aún no ha persistido el token
            val bearerToken = "Bearer ${body.token}"
            
            val profileIdSaved = try {
                if (body.role == UserRole.STUDENT) {
                    val profileRes = api.registerStudent(
                        body.userId, 
                        StudentRequest("Bachiller", "Intereses generales"), 
                        bearerToken
                    )
                    if (profileRes.isSuccessful) {
                        val pId = profileRes.body()?.id
                        if (pId != null) {
                            sessionManager.saveProfileId(pId)
                            android.util.Log.d("AuthRepository", "Perfil Estudiante creado con ID: $pId")
                            true
                        } else {
                            android.util.Log.e("AuthRepository", "Perfil Estudiante creado pero ID es NULL")
                            false
                        }
                    } else {
                        android.util.Log.e("AuthRepository", "Error API registerStudent: ${profileRes.code()} - ${profileRes.errorBody()?.string()}")
                        false
                    }
                } else {
                    val profileRes = api.registerTutor(
                        body.userId, 
                        TutorRequest("Especialidad pendiente", "Sin descripción", 10.0, active = false),
                        bearerToken
                    )
                    if (profileRes.isSuccessful) {
                        val pId = profileRes.body()?.id
                        if (pId != null) {
                            // SEGURIDAD CRÍTICA: Forzamos el estado inactivo inmediatamente después de la creación
                            // para asegurar que no sea visible hasta que el administrador lo apruebe.
                            try {
                                api.changeTutorStatus(pId, active = false, token = bearerToken)
                            } catch (e: Exception) {
                                // Logueamos pero continuamos para no romper el flujo del usuario
                                android.util.Log.e("AuthRepository", "Error al forzar inactividad: ${e.message}")
                            }
                            
                            sessionManager.saveProfileId(pId)
                            android.util.Log.d("AuthRepository", "Perfil Tutor creado e inactivado con ID: $pId")
                            true
                        } else {
                            android.util.Log.e("AuthRepository", "Perfil Tutor creado pero ID es NULL")
                            false
                        }
                    } else {
                        android.util.Log.e("AuthRepository", "Error API registerTutor: ${profileRes.code()} - ${profileRes.errorBody()?.string()}")
                        false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Excepción creando perfil: ${e.message}", e)
                false
            }

            if (profileIdSaved) {
                emit(result)
            } else {
                // Re-intentar obtener el ID si la creación falló o no devolvió el ID (tal vez ya existía)
                fetchAndSaveProfileId(body.userId, body.role, bearerToken)
                val finalId = sessionManager.profileId.first() // Usar first() para esperar el valor
                if (finalId != null) {
                    emit(result)
                } else {
                    emit(Resource.Error("El usuario se creó correctamente, pero no pudimos inicializar tu perfil. Por favor, intenta iniciar sesión de nuevo."))
                }
            }
        } else {
            emit(result)
        }
    }

    /**
     * Intenta recuperar y guardar el profileId si falta.
     */
    suspend fun refreshProfileId() {
        val uId = sessionManager.userId.first()
        val roleStr = sessionManager.userRole.first()
        val token = sessionManager.authToken.first()
        
        if (uId != null && roleStr != null) {
            val role = try { UserRole.valueOf(roleStr) } catch(e: Exception) { null }
            role?.let {
                fetchAndSaveProfileId(uId, it, token?.let { t -> "Bearer $t" })
            }
        }
    }

    suspend fun fetchAndSaveProfileId(userId: Long, role: UserRole, token: String? = null) {
        val email = sessionManager.userEmail.firstOrNull()
        android.util.Log.d("AuthRepository", "Iniciando recuperación de perfil por email: $email")

        try {
            // Dado que los endpoints /user/{userId} no existen, vamos directo al fallback por Email
            if (!email.isNullOrBlank()) {
                when (role) {
                    UserRole.STUDENT -> {
                        val all = api.getAllStudents(token)
                        if (all.isSuccessful) {
                            val profile = all.body()?.find { it.email.equals(email, ignoreCase = true) }
                            profile?.id?.let { 
                                sessionManager.saveProfileId(it)
                                android.util.Log.d("AuthRepository", "Perfil Estudiante encontrado por Email: $it")
                            }
                        }
                    }
                    UserRole.TUTOR -> {
                        val all = api.getAllTutors(token)
                        if (all.isSuccessful) {
                            val profile = all.body()?.find { it.email.equals(email, ignoreCase = true) }
                            profile?.id?.let { 
                                sessionManager.saveProfileId(it)
                                android.util.Log.d("AuthRepository", "Perfil Tutor encontrado por Email: $it")
                            }
                        }
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Fallo catastrófico en recuperación de perfil: ${e.message}")
        }
    }

    suspend fun logout() = sessionManager.clearSession()
}
