package com.tutorlink.app.viewmodel.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.TutorRequest
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorProfileViewModel @Inject constructor(
    private val tutorRepository: TutorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<TutorResponse>>(Resource.Idle())
    val profile: StateFlow<Resource<TutorResponse>> = _profile.asStateFlow()

    private val _updateResult = MutableStateFlow<Resource<TutorResponse>?>(null)
    val updateResult: StateFlow<Resource<TutorResponse>?> = _updateResult.asStateFlow()

    init { loadProfile() }

    fun loadProfile() = viewModelScope.launch {
        val profileId = sessionManager.profileId.first() ?: return@launch
        tutorRepository.getTutorById(profileId).collect { _profile.value = it }
    }

    fun updateProfile(specialty: String, description: String?, hourlyRate: Double) = viewModelScope.launch {
        val profileId = sessionManager.profileId.first() ?: return@launch
        tutorRepository.updateTutor(profileId, TutorRequest(specialty, description, hourlyRate))
            .collect { _updateResult.value = it }
    }

    fun clearUpdateResult() { _updateResult.value = null }
}
