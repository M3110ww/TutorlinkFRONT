package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.ReviewRequest
import com.tutorlink.app.data.remote.dto.ReviewResponse
import com.tutorlink.app.repository.ReviewRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _result = MutableStateFlow<Resource<ReviewResponse>?>(null)
    val result: StateFlow<Resource<ReviewResponse>?> = _result.asStateFlow()

    fun submitReview(sessionId: Long, rating: Int, comment: String?) = viewModelScope.launch {
        val studentId = sessionManager.profileId.first() ?: run {
            _result.value = Resource.Error("Perfil de estudiante no encontrado")
            return@launch
        }
        reviewRepository.createReview(sessionId, studentId, ReviewRequest(rating, comment?.ifBlank { null }))
            .collect { _result.value = it }
    }

    fun clearResult() { _result.value = null }
}
