package com.tutorlink.app.viewmodel.tutor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.AvailabilityRequest
import com.tutorlink.app.data.remote.dto.AvailabilityResponse
import com.tutorlink.app.repository.AvailabilityRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorAvailabilityViewModel @Inject constructor(
    private val availabilityRepository: AvailabilityRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _availability = MutableStateFlow<Resource<List<AvailabilityResponse>>>(Resource.Idle())
    val availability: StateFlow<Resource<List<AvailabilityResponse>>> = _availability.asStateFlow()

    var isAddDialogOpen by mutableStateOf(false)
        private set

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    private var cachedTutorId: Long? = null

    init { getAvailability() }

    fun getAvailability() = viewModelScope.launch {
        // FIXED: profileId = Tutor.id (tutors table), NOT userId
        val tutorId = cachedTutorId ?: sessionManager.profileId.first()
        cachedTutorId = tutorId
        if (tutorId == null) {
            _availability.value = Resource.Error("Perfil de tutor no encontrado.")
            return@launch
        }
        availabilityRepository.getByTutor(tutorId).collect { _availability.value = it }
    }

    fun addSlot(dayOfWeek: Int, startTime: String, endTime: String) = viewModelScope.launch {
        val tutorId = cachedTutorId ?: return@launch
        
        // Aseguramos formato HH:mm:ss para el backend
        val formattedStart = if (startTime.count { it == ':' } == 1) "$startTime:00" else startTime
        val formattedEnd = if (endTime.count { it == ':' } == 1) "$endTime:00" else endTime

        val request = AvailabilityRequest(
            dayOfWeek = dayOfWeek,
            startTime = formattedStart,
            endTime   = formattedEnd,
            recurring = true
        )
        availabilityRepository.add(tutorId, request).collect { result ->
            when (result) {
                is Resource.Success -> { 
                    _actionResult.value = "Horario agregado"
                    isAddDialogOpen = false
                    getAvailability() 
                }
                is Resource.Error   -> _actionResult.value = result.message
                else -> Unit
            }
        }
    }

    fun deleteSlot(slotId: Long) = viewModelScope.launch {
        availabilityRepository.delete(slotId).collect { result ->
            when (result) {
                is Resource.Success -> { _actionResult.value = "Horario eliminado"; getAvailability() }
                is Resource.Error   -> _actionResult.value = result.message
                else -> Unit
            }
        }
    }

    fun clearActionResult() { _actionResult.value = null }

    fun showAddDialog() { isAddDialogOpen = true }
    fun hideAddDialog() { isAddDialogOpen = false }
}