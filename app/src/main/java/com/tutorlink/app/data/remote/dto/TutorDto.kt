package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TutorRequest(
    @SerializedName("specialty")   val specialty: String,
    @SerializedName("description") val description: String?,
    @SerializedName("hourlyRate")  val hourlyRate: Double,
    @SerializedName("active")      val active: Boolean = false
)

data class TutorResponse(
    @SerializedName("id")            val id: Long,
    @SerializedName("tutorName")     val tutorName: String,
    @SerializedName("active")        val active: Boolean,
    @SerializedName("email")         val email: String,
    @SerializedName("description")   val description: String?,
    @SerializedName("hourlyRate")    val hourlyRate: Double,
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("specialty")     val specialty: String?
)