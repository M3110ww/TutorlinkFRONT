package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AvailabilityRequest(
    @SerializedName("dayOfWeek")  val dayOfWeek: Int,
    @SerializedName("startTime")  val startTime: String,   // "HH:mm:ss"
    @SerializedName("endTime")    val endTime: String,
    @SerializedName("recurring")  val recurring: Boolean = true
)

data class AvailabilityResponse(
    @SerializedName("id")          val id: Long? = null,
    @SerializedName("dayOfWeek")   val dayOfWeek: Int? = null,
    @SerializedName("startTime")   val startTime: String? = null,
    @SerializedName("endTime")     val endTime: String? = null,
    @SerializedName("recurring")   val recurring: Boolean? = null,
    @SerializedName("occupied")    val occupied: Boolean? = null
)