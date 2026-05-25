package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.data.remote.dto.SessionStatus
import com.tutorlink.app.repository.AuthRepository
import com.tutorlink.app.repository.SessionRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentSessionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _sessions = MutableStateFlow<Resource<List<SessionResponse>>>(Resource.Idle())
    
    private val _filter = MutableStateFlow<SessionStatus?>(null)
    val filter: StateFlow<SessionStatus?> = _filter.asStateFlow()

    val sessions: StateFlow<Resource<List<SessionResponse>>> = combine(_sessions, _filter) { res, f ->
        if (res is Resource.Success && f != null) {
            Resource.Success(res.data?.filter { it.status == f } ?: emptyList())
        } else {
            res
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Idle())

    init { getSessions() }

    fun setFilter(status: SessionStatus?) {
        _filter.value = status
    }

    fun getSessions() = viewModelScope.launch {
        _sessions.value = Resource.Loading()
        
        var studentId = sessionManager.profileId.first()
        
        if (studentId == null) {
            authRepository.refreshProfileId()
            studentId = sessionManager.profileId.first()
        }

        if (studentId == null) {
            _sessions.value = Resource.Error(
                "Perfil de estudiante no encontrado. Completa tu registro primero."
            )
            return@launch
        }

        sessionRepository.getStudentSessions(studentId).collect { _sessions.value = it }
    }

    fun cancelSession(sessionId: Long) = viewModelScope.launch {
        sessionRepository.cancelSession(sessionId).collect { result ->
            if (result is Resource.Success) getSessions()
        }
    }
}
