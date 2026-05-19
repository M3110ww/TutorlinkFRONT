package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.remote.dto.AvailabilityResponse
import com.tutorlink.app.data.remote.dto.ReviewResponse
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.AvailabilityRepository
import com.tutorlink.app.repository.ReviewRepository
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class TutorDetailViewModel @Inject constructor(
    private val tutorRepository: TutorRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _tutor = MutableStateFlow<Resource<TutorResponse>>(Resource.Loading())
    val tutor: StateFlow<Resource<TutorResponse>> = _tutor

    private val _availability = MutableStateFlow<Resource<List<AvailabilityResponse>>>(Resource.Loading())
    val availability: StateFlow<Resource<List<AvailabilityResponse>>> = _availability

    private val _reviews = MutableStateFlow<Resource<List<ReviewResponse>>>(Resource.Loading())
    val reviews: StateFlow<Resource<List<ReviewResponse>>> = _reviews

    fun getTutorDetail(tutorId: Long) {
        tutorRepository.getTutorById(tutorId).onEach { result ->
            _tutor.value = result
        }.launchIn(viewModelScope)

        getTutorAvailability(tutorId)
        getTutorReviews(tutorId)
    }

    fun getTutorAvailability(tutorId: Long) {
        _availability.value = Resource.Loading()
        
        availabilityRepository.getAvailable(tutorId).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    // Si hay CUALQUIER error (403, 404, o error de red), 
                    // intentamos el endpoint general como respaldo de seguridad.
                    getTutorAvailabilityFallback(tutorId)
                }
                is Resource.Success -> {
                    // Si el servidor responde con éxito pero la lista está vacía,
                    // también probamos el fallback por si acaso.
                    if (result.data.isNullOrEmpty()) {
                        getTutorAvailabilityFallback(tutorId)
                    } else {
                        _availability.value = result
                    }
                }
                is Resource.Loading -> {
                    _availability.value = Resource.Loading()
                }
                else -> { /* No hacemos nada para no sobreescribir el fallback */ }
            }
        }.launchIn(viewModelScope)
    }

    private fun getTutorAvailabilityFallback(tutorId: Long) {
        availabilityRepository.getByTutor(tutorId).onEach { result ->
            // Solo actualizamos si no estamos ya en un estado de éxito del primer llamado
            if (_availability.value !is Resource.Success) {
                _availability.value = result
            }
        }.launchIn(viewModelScope)
    }

    fun getTutorReviews(tutorId: Long) {
        reviewRepository.getReviewsByTutor(tutorId).onEach { result ->
            _reviews.value = result
        }.launchIn(viewModelScope)
    }
}
