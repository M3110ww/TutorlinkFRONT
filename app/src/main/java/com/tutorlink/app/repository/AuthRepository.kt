package com.tutorlink.app.repository

import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.*
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: TutoApiService,
    private val sessionManager: SessionManager
) {
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
            // Save base session first (token is needed for profile fetch)
            sessionManager.saveSession(
                token  = body.token,
                role   = body.role.name,
                name   = body.userName,
                userId = body.userId,
                email  = request.email
            )
            // FIXED: Fetch and save profile ID (student/tutor table ID)
            fetchAndSaveProfileId(body.userId, body.role)
        }
        emit(result)
    }

    fun register(request: RegisterRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        val result = safeCall { api.register(request) }
        if (result is Resource.Success) {
            val body = result.data!!
            // FIXED: No login fallback needed — backend always returns AuthResponse on register
            sessionManager.saveSession(
                token  = body.token,
                role   = body.role.name,
                name   = body.userName,
                userId = body.userId,
                email  = request.email
            )
            fetchAndSaveProfileId(body.userId, body.role)
        }
        emit(result)
    }

    /**
     * CRITICAL FIX: After login, fetch the profile from students/tutors table
     * to get the correct profileId needed for session endpoints.
     */
    private suspend fun fetchAndSaveProfileId(userId: Long, role: UserRole) {
        try {
            val savedEmail = sessionManager.userEmail.firstOrNull()
            
            when (role) {
                UserRole.STUDENT -> {
                    val directRes = try { api.getStudentByUserId(userId) } catch (e: Exception) { null }
                    if (directRes != null && directRes.isSuccessful && directRes.body() != null) {
                        sessionManager.saveProfileId(directRes.body()!!.id)
                    } else if (savedEmail != null) {
                        // Fallback: Filter all students by email
                        val listRes = try { api.getAllStudents() } catch (e: Exception) { null }
                        if (listRes != null && listRes.isSuccessful && listRes.body() != null) {
                            val student = listRes.body()!!.find { it.email == savedEmail }
                            if (student != null) {
                                sessionManager.saveProfileId(student.id)
                            }
                        }
                    }
                }
                UserRole.TUTOR -> {
                    val directRes = try { api.getTutorByUserId(userId) } catch (e: Exception) { null }
                    if (directRes != null && directRes.isSuccessful && directRes.body() != null) {
                        sessionManager.saveProfileId(directRes.body()!!.id)
                    } else if (savedEmail != null) {
                        // Fallback: Filter all tutors by email
                        val listRes = try { api.getAllTutors() } catch (e: Exception) { null }
                        if (listRes != null && listRes.isSuccessful && listRes.body() != null) {
                            val tutor = listRes.body()!!.find { it.email == savedEmail }
                            if (tutor != null) {
                                sessionManager.saveProfileId(tutor.id)
                            }
                        }
                    }
                }
                UserRole.ADMIN -> { /* Admin uses userId directly */ }
            }
        } catch (_: Exception) {
            // Non-fatal, simply does not save profileId
        }
    }

    suspend fun logout() = sessionManager.clearSession()
}
