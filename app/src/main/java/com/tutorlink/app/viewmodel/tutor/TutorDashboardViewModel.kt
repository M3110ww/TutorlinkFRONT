package com.tutorlink.app.viewmodel.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.AuthRepository
import com.tutorlink.app.repository.SessionRepository
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorDashboardViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val tutorRepository: TutorRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _sessions = MutableStateFlow<Resource<List<SessionResponse>>>(Resource.Idle())
    val sessions: StateFlow<Resource<List<SessionResponse>>> = _sessions.asStateFlow()

    private val _tutorProfile = MutableStateFlow<Resource<TutorResponse>>(Resource.Idle())
    val tutorProfile: StateFlow<Resource<TutorResponse>> = _tutorProfile.asStateFlow()

    init { 
        observeProfileId()
    }

    private fun observeProfileId() {
        sessionManager.profileId
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { id ->
                getTutorProfileById(id)
                getTutorSessionsById(id)
            }
            .launchIn(viewModelScope)
    }

    fun getTutorProfile() {
        viewModelScope.launch {
            val id = sessionManager.profileId.first()
            if (id != null) getTutorProfileById(id)
            else authRepository.refreshProfileId()
        }
    }

    private fun getTutorProfileById(id: Long) = viewModelScope.launch {
        _tutorProfile.value = Resource.Loading()
        tutorRepository.getTutorById(id).collect { _tutorProfile.value = it }
    }

    fun getTutorSessions() {
        viewModelScope.launch {
            val id = sessionManager.profileId.first()
            if (id != null) getTutorSessionsById(id)
        }
    }

    private fun getTutorSessionsById(id: Long) = viewModelScope.launch {
        _sessions.value = Resource.Loading()
        sessionRepository.getTutorSessions(id).collect { _sessions.value = it }
    }

    fun confirmSession(sessionId: Long) = viewModelScope.launch {
        sessionRepository.confirmSession(sessionId).collect { result ->
            if (result is Resource.Success) getTutorSessions()
        }
    }

    fun cancelSession(sessionId: Long) = viewModelScope.launch {
        sessionRepository.cancelSession(sessionId).collect { result ->
            if (result is Resource.Success) getTutorSessions()
        }
    }

    fun completeSession(sessionId: Long) = viewModelScope.launch {
        sessionRepository.completeSession(sessionId).collect { result ->
            if (result is Resource.Success) getTutorSessions()
        }
    }
}