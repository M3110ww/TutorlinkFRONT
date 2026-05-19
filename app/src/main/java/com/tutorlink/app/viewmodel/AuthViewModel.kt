package com.tutorlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.remote.dto.AuthResponse
import com.tutorlink.app.data.remote.dto.LoginRequest
import com.tutorlink.app.data.remote.dto.RegisterRequest
import com.tutorlink.app.repository.AuthRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val userRole: StateFlow<String?> = repository.userRole
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userName: StateFlow<String?> = repository.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userId: StateFlow<Long?> = repository.userId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * FIXED: profileId is the ID from the students/tutors table,
     * not the userId from the users table. Use this for session endpoints.
     */
    val profileId: StateFlow<Long?> = repository.profileId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * FIXED: True once DataStore has emitted its first value.
     * SplashScreen waits for this before routing — avoids race condition
     * where userRole is null just because DataStore hasn't loaded yet.
     */
    val isSessionReady: StateFlow<Boolean> = repository.authToken
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _authState = MutableStateFlow<Resource<AuthResponse>>(Resource.Idle())
    val authState: StateFlow<Resource<AuthResponse>> = _authState.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        repository.login(LoginRequest(email.trim(), password))
            .collect { _authState.value = it }
    }

    fun register(request: RegisterRequest) = viewModelScope.launch {
        repository.register(request).collect { _authState.value = it }
    }

    fun logout() = viewModelScope.launch {
        repository.logout()
        _authState.value = Resource.Idle()
    }

    fun resetState() { _authState.value = Resource.Idle() }
}