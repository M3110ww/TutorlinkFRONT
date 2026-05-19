package com.tutorlink.app.repository

import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.AvailabilityRequest
import com.tutorlink.app.data.remote.dto.AvailabilityResponse
import com.tutorlink.app.repository.flowSafeCall
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityRepository @Inject constructor(
    private val api: TutoApiService
) {
    fun getByTutor(tutorId: Long): Flow<Resource<List<AvailabilityResponse>>> =
        flowSafeCall { api.getTutorAvailability(tutorId) }

    fun getAvailable(tutorId: Long): Flow<Resource<List<AvailabilityResponse>>> =
        flowSafeCall { api.getAvailableTutorSlots(tutorId) }

    fun add(tutorId: Long, request: AvailabilityRequest): Flow<Resource<AvailabilityResponse>> =
        flowSafeCall { api.addAvailability(tutorId, request) }

    fun delete(id: Long): Flow<Resource<Unit>> =
        flowSafeCall { api.deleteAvailability(id) }
}
