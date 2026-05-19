package com.tutorlink.app.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.remote.dto.*
import com.tutorlink.app.repository.AdminRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<Map<String, Double>>>(Resource.Idle())
    val stats: StateFlow<Resource<Map<String, Double>>> = _stats.asStateFlow()

    private val _sessions = MutableStateFlow<Resource<List<SessionResponse>>>(Resource.Idle())
    val sessions: StateFlow<Resource<List<SessionResponse>>> = _sessions.asStateFlow()

    private val _tutors = MutableStateFlow<Resource<List<TutorResponse>>>(Resource.Idle())
    val tutors: StateFlow<Resource<List<TutorResponse>>> = _tutors.asStateFlow()

    private val _students = MutableStateFlow<Resource<List<StudentResponse>>>(Resource.Idle())
    val students: StateFlow<Resource<List<StudentResponse>>> = _students.asStateFlow()

    private val _updateStatus = MutableStateFlow<Resource<Any>>(Resource.Idle())
    val updateStatus: StateFlow<Resource<Any>> = _updateStatus.asStateFlow()

    init {
        getStats()
        getSessions()
        getTutors()
        getStudents()
    }

    fun getStats() = viewModelScope.launch {
        repository.getStats().collect { _stats.value = it }
    }

    fun getSessions() = viewModelScope.launch {
        repository.getAllSessions().collect { _sessions.value = it }
    }

    fun getTutors() = viewModelScope.launch {
        repository.getAllTutors().collect { _tutors.value = it }
    }

    fun getStudents() = viewModelScope.launch {
        repository.getAllStudents().collect { _students.value = it }
    }

    fun confirmSession(id: Long) = viewModelScope.launch {
        repository.confirmSession(id).collect { result ->
            if (result is Resource.Success) getSessions()
        }
    }

    fun cancelSession(id: Long) = viewModelScope.launch {
        repository.cancelSession(id).collect { result ->
            if (result is Resource.Success) getSessions()
        }
    }

    fun toggleTutorStatus(id: Long, active: Boolean) = viewModelScope.launch {
        repository.changeTutorStatus(id, active).collect { result ->
            if (result is Resource.Success) getTutors()
        }
    }

    fun updateTutor(id: Long, specialty: String, hourlyRate: Double, description: String?) = viewModelScope.launch {
        _updateStatus.value = Resource.Loading()
        repository.updateTutor(id, TutorRequest(specialty, description, hourlyRate)).collect { result ->
            if (result is Resource.Success) {
                getTutors()
                _updateStatus.value = Resource.Success(Unit)
            } else if (result is Resource.Error) {
                _updateStatus.value = Resource.Error(result.message ?: "Error al actualizar")
            }
        }
    }

    fun updateStudent(id: Long, academicLevel: String, interest: String?) = viewModelScope.launch {
        _updateStatus.value = Resource.Loading()
        repository.updateStudent(id, StudentRequest(academicLevel, interest)).collect { result ->
            if (result is Resource.Success) {
                getStudents()
                _updateStatus.value = Resource.Success(Unit)
            } else if (result is Resource.Error) {
                _updateStatus.value = Resource.Error(result.message ?: "Error al actualizar")
            }
        }
    }

    fun clearUpdateStatus() {
        _updateStatus.value = Resource.Idle()
    }
}
