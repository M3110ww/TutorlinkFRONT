package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.StudentRequest
import com.tutorlink.app.data.remote.dto.StudentResponse
import com.tutorlink.app.repository.StudentRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<StudentResponse>>(Resource.Idle())
    val profile: StateFlow<Resource<StudentResponse>> = _profile.asStateFlow()

    private val _updateResult = MutableStateFlow<Resource<StudentResponse>?>(null)
    val updateResult: StateFlow<Resource<StudentResponse>?> = _updateResult.asStateFlow()

    init { loadProfile() }

    fun loadProfile() = viewModelScope.launch {
        val profileId = sessionManager.profileId.first() ?: return@launch
        studentRepository.getStudentById(profileId).collect { _profile.value = it }
    }

    fun updateProfile(academicLevel: String, interests: String?) = viewModelScope.launch {
        val profileId = sessionManager.profileId.first() ?: return@launch
        studentRepository.updateStudent(profileId, StudentRequest(academicLevel, interests))
            .collect { _updateResult.value = it }
    }

    fun clearUpdateResult() { _updateResult.value = null }
}
