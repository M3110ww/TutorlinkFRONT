package com.tutorlink.app.viewmodel.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.repository.AuthRepository
import com.tutorlink.app.repository.SessionRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorDashboardViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _sessions = MutableStateFlow<Resource<List<SessionResponse>>>(Resource.Idle())
    val sessions: StateFlow<Resource<List<SessionResponse>>> = _sessions.asStateFlow()

    init { getTutorSessions() }

    fun getTutorSessions() = viewModelScope.launch {
        _sessions.value = Resource.Loading()
        
        var tutorId = sessionManager.profileId.first()
        
        // RECOVERY LOGIC: Si no hay ID, intentamos recuperarlo forzosamente
        if (tutorId == null) {
            authRepository.refreshProfileId()
            tutorId = sessionManager.profileId.first()
        }

        if (tutorId == null) {
            _sessions.value = Resource.Error(
                "Perfil de tutor no encontrado. Completa tu registro."
            )
            return@launch
        }

        sessionRepository.getTutorSessions(tutorId).collect { _sessions.value = it }
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