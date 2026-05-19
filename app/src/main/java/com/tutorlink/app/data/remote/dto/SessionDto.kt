package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

enum class SessionStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

enum class SessionModality {
    VIRTUAL, IN_PERSON
}

/**
 * FIXED: scheduledAt is String in ISO-8601 without timezone.
 * Backend SesionRequest.scheduledAt is LocalDateTime.
 * Jackson on the backend deserializes "2024-11-15T10:00:00" correctly.
 * DO NOT include timezone suffix (+00:00 or Z) — Spring will reject it.
 * Use LocalDateTime formatter in BookSessionViewModel.
 */
data class SessionRequest(
    @SerializedName("tutorId")          val tutorId: Long,
    @SerializedName("scheduledAt")      val scheduledAt: String,
    @SerializedName("durationMinutes")  val durationMinutes: Int,
    @SerializedName("modality")         val modality: SessionModality,
    @SerializedName("meetingLink")      val meetingLink: String?
)

/**
 * FIXED: scheduledAt comes from backend as a LocalDateTime serialized by Jackson.
 * With spring.jackson.serialization.write-dates-as-timestamps=false
 * it arrives as "2024-11-15T10:00:00" (ISO string).
 * If timestamps mode is ON, it arrives as [2024,11,15,10,0,0] (array) — use String
 * and handle both cases in the UI layer.
 */
data class SessionResponse(
    @SerializedName("id")              val id: Long,
    @SerializedName("tutorName")       val tutorName: String,
    @SerializedName("studentName")     val studentName: String,
    @SerializedName("subject")         val subject: String?,
    @SerializedName("scheduledAt")     val scheduledAt: String,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("totalCost")       val totalCost: Double,
    @SerializedName("status")          val status: SessionStatus,
    @SerializedName("modality")        val modality: SessionModality,
    @SerializedName("meetingLink")     val meetingLink: String?,
    @SerializedName("createdAt")       val createdAt: String?
)
