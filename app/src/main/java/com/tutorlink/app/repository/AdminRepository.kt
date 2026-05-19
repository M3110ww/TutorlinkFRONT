package com.tutorlink.app.repository

import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.*
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val api: TutoApiService
) {
    fun getStats(): Flow<Resource<Map<String, Double>>> =
        flowSafeCall { api.getAdminStats() }

    fun getAllSessions(): Flow<Resource<List<SessionResponse>>> =
        flowSafeCall { api.getAdminSessions() }

    fun getAllTutors(): Flow<Resource<List<TutorResponse>>> =
        flowSafeCall { api.getAdminTutors() }

    fun getAllStudents(): Flow<Resource<List<StudentResponse>>> =
        flowSafeCall { api.getAllStudents() }

    fun confirmSession(id: Long): Flow<Resource<SessionResponse>> =
        flowSafeCall { api.adminConfirmSession(id) }

    fun cancelSession(id: Long): Flow<Resource<SessionResponse>> =
        flowSafeCall { api.adminCancelSession(id) }

    fun changeTutorStatus(id: Long, active: Boolean): Flow<Resource<TutorResponse>> =
        flowSafeCall { api.adminChangeTutorStatus(id, active) }

    fun updateTutor(id: Long, request: TutorRequest): Flow<Resource<TutorResponse>> =
        flowSafeCall { api.updateTutor(id, request) }

    fun updateStudent(id: Long, request: StudentRequest): Flow<Resource<StudentResponse>> =
        flowSafeCall { api.updateStudent(id, request) }
}
