package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.AuthRepository
import com.tutorlink.app.repository.SessionRepository
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val tutorRepository: TutorRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _featuredTutors = MutableStateFlow<Resource<List<TutorResponse>>>(Resource.Idle())
    val featuredTutors: StateFlow<Resource<List<TutorResponse>>> = _featuredTutors.asStateFlow()

    private val _sessions = MutableStateFlow<Resource<List<SessionResponse>>>(Resource.Idle())
    val sessions: StateFlow<Resource<List<SessionResponse>>> = _sessions.asStateFlow()

    init {
        getFeaturedTutors()
        getStudentSessions()
    }

    fun getFeaturedTutors() {
        viewModelScope.launch {
            tutorRepository.getActiveTutors().collect {
                _featuredTutors.value = it
            }
        }
    }

    fun getStudentSessions() {
        viewModelScope.launch {
            authRepository.profileId.collectLatest { id ->
                id?.let {
                    sessionRepository.getStudentSessions(it).collect { result ->
                        _sessions.value = result
                    }
                }
            }
        }
    }
}
