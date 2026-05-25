package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.local.SessionManager
import com.tutorlink.app.data.remote.dto.SessionModality
import com.tutorlink.app.data.remote.dto.SessionRequest
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.SessionRepository
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BookSessionViewModel @Inject constructor(
    private val repository: SessionRepository,
    private val tutorRepository: TutorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _bookingResult = MutableStateFlow<Resource<SessionResponse>?>(null)
    val bookingResult: StateFlow<Resource<SessionResponse>?> = _bookingResult.asStateFlow()

    private val _tutorInfo = MutableStateFlow<TutorResponse?>(null)
    val tutorInfo: StateFlow<TutorResponse?> = _tutorInfo.asStateFlow()

    fun loadTutorInfo(tutorId: Long) = viewModelScope.launch {
        tutorRepository.getTutorById(tutorId).collect { result ->
            if (result is Resource.Success) _tutorInfo.value = result.data
        }
    }

    /**
     * FIXED #1: Uses profileId (Estudiante.id) not userId (Usuario.id).
     *
     * FIXED #2: scheduledAt formatted as ISO-8601 without timezone.
     * Backend SesionRequest.scheduledAt is LocalDateTime — Jackson expects
     * "2024-11-15T10:00:00" without Z or +00:00.
     *
     * @param scheduledAt ISO-8601 string WITHOUT timezone: "2024-11-15T10:00:00"
     */
    fun bookSession(
        tutorId: Long,
        scheduledAt: String,
        durationMinutes: Int,
        modality: SessionModality,
        meetingLink: String? = null
    ) = viewModelScope.launch {
        // VALIDACIÓN: No permitir fechas pasadas
        try {
            val selectedDate = LocalDateTime.parse(scheduledAt)
            if (selectedDate.isBefore(LocalDateTime.now())) {
                _bookingResult.value = Resource.Error("No puedes programar una sesión en una fecha o hora que ya pasó.")
                return@launch
            }
        } catch (e: Exception) {
            // Si el formato es inválido, dejamos que el backend lo maneje, 
            // pero lo ideal es que el selector de fecha del UI envíe ISO correcto.
        }

        // FIXED: profileId not userId
        val studentId = sessionManager.profileId.first()
        if (studentId == null) {
            _bookingResult.value = Resource.Error(
                "Perfil de estudiante no encontrado. Completa tu registro."
            )
            return@launch
        }

        // FIXED: Ensure no timezone in the datetime string
        val cleanDateTime = sanitizeDateTime(scheduledAt)

        val request = SessionRequest(
            tutorId         = tutorId,
            scheduledAt     = cleanDateTime,
            durationMinutes = durationMinutes,
            modality        = modality,
            meetingLink     = meetingLink
        )
        repository.bookSession(studentId, request).collect { _bookingResult.value = it }
    }

    /**
     * Ensures the datetime is in "yyyy-MM-dd'T'HH:mm:ss" format.
     * Strips any timezone suffix (Z, +HH:mm, -HH:mm) that would cause
     * Spring's LocalDateTime deserializer to throw 400 Bad Request.
     */
    private fun sanitizeDateTime(raw: String): String {
        return try {
            // Parse whatever format was given
            val dt = LocalDateTime.parse(raw.substringBefore("+").substringBefore("Z").trim())
            dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        } catch (_: Exception) {
            // If parsing fails, strip timezone chars manually
            raw.substringBefore("+").substringBefore("Z").trim()
        }
    }

    fun reset() { _bookingResult.value = null }
}