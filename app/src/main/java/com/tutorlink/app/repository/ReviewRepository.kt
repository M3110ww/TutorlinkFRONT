package com.tutorlink.app.repository

import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.ReviewRequest
import com.tutorlink.app.data.remote.dto.ReviewResponse
import com.tutorlink.app.repository.flowSafeCall
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val api: TutoApiService
) {
    fun getReviewsByTutor(tutorId: Long): Flow<Resource<List<ReviewResponse>>> =
        flowSafeCall { api.getReviewsByTutor(tutorId) }

    fun createReview(sessionId: Long, studentId: Long, request: ReviewRequest): Flow<Resource<ReviewResponse>> =
        flowSafeCall { api.createReview(sessionId, studentId, request) }
}
