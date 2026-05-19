package com.tutorlink.app.repository

import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.AvailabilityResponse
import com.tutorlink.app.data.remote.dto.SessionRequest
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(private val api: TutoApiService) {

    /**
     * FIXED: studentId here must be the Estudiante.id (students table),
     * NOT the Usuario.id (users table).
     * Use sessionManager.profileId, NOT sessionManager.userId.
     */
    fun getStudentSessions(studentId: Long): Flow<Resource<List<SessionResponse>>> =
        flowSafeCall { api.getStudentSessions(studentId) }

    /**
     * FIXED: tutorId here must be the Tutor.id (tutors table),
     * NOT the Usuario.id (users table).
     * Use sessionManager.profileId, NOT sessionManager.userId.
     */
    fun getTutorSessions(tutorId: Long): Flow<Resource<List<SessionResponse>>> =
        flowSafeCall { api.getTutorSessions(tutorId) }

    fun bookSession(studentId: Long, request: SessionRequest): Flow<Resource<SessionResponse>> =
        flowSafeCall { api.bookSession(studentId, request) }

    fun confirmSession(sessionId: Long): Flow<Resource<SessionResponse>> =
        flowSafeCall { api.confirmSession(sessionId) }

    fun cancelSession(sessionId: Long): Flow<Resource<SessionResponse>> =
        flowSafeCall { api.cancelSession(sessionId) }

    fun completeSession(sessionId: Long): Flow<Resource<SessionResponse>> =
        flowSafeCall { api.completeSession(sessionId) }

    fun getTutorAvailability(tutorId: Long): Flow<Resource<List<AvailabilityResponse>>> =
        flowSafeCall { api.getAvailableTutorSlots(tutorId) }
}