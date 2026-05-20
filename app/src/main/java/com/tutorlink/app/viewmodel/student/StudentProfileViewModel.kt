package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.StudentRequest
import com.tutorlink.app.data.remote.dto.StudentResponse
import com.tutorlink.app.repository.AuthRepository
import com.tutorlink.app.repository.StudentRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<StudentResponse>>(Resource.Idle())
    val profile: StateFlow<Resource<StudentResponse>> = _profile.asStateFlow()

    private val _updateResult = MutableStateFlow<Resource<StudentResponse>?>(null)
    val updateResult: StateFlow<Resource<StudentResponse>?> = _updateResult.asStateFlow()

    init {
        // Observar reactivamente el profileId para cargar el perfil en cuanto esté disponible
        sessionManager.profileId
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { id ->
                loadProfileById(id)
            }
            .launchIn(viewModelScope)
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = Resource.Loading()
            var id = sessionManager.profileId.first()
            
            if (id == null) {
                authRepository.refreshProfileId()
                id = sessionManager.profileId.first()
            }

            if (id != null) loadProfileById(id)
            else _profile.value = Resource.Error("No se encontró el ID del perfil.")
        }
    }

    private suspend fun loadProfileById(id: Long) {
        studentRepository.getStudentById(id).collect { _profile.value = it }
    }

    fun updateProfile(academicLevel: String, interests: String?) = viewModelScope.launch {
        _updateResult.value = Resource.Loading()
        var profileId = sessionManager.profileId.first()
        
        if (profileId == null) {
            authRepository.refreshProfileId()
            profileId = sessionManager.profileId.first()
        }

        if (profileId == null) {
            _updateResult.value = Resource.Error("Error: No se encontró el ID del perfil. Intenta re-iniciar sesión.")
            return@launch
        }
        studentRepository.updateStudent(profileId, StudentRequest(academicLevel, interests))
            .collect { _updateResult.value = it }
    }

    fun clearUpdateResult() { _updateResult.value = null }
}
