package com.tutorlink.app.repository

import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.TutorRequest
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorRepository @Inject constructor(
    private val api: TutoApiService
) {
    fun getActiveTutors(): Flow<Resource<List<TutorResponse>>> =
        flowSafeCall { api.getActiveTutors() }

    fun getTutorById(id: Long): Flow<Resource<TutorResponse>> =
        flowSafeCall { api.getTutorById(id) }

    fun searchTutors(specialty: String): Flow<Resource<List<TutorResponse>>> =
        flowSafeCall { api.searchTutors(specialty) }

    fun registerTutor(userId: Long, req: TutorRequest): Flow<Resource<TutorResponse>> =
        flowSafeCall { api.registerTutor(userId, req) }

    fun updateTutor(id: Long, req: TutorRequest): Flow<Resource<TutorResponse>> =
        flowSafeCall { api.updateTutor(id, req) }
}
