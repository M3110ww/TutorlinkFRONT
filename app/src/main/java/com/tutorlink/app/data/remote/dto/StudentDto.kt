package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StudentRequest(
    @SerializedName("academicLevel") val academicLevel: String,
    @SerializedName("interests")     val interests: String?
)

data class StudentResponse(
    @SerializedName("id")            val id: Long,
    @SerializedName("studentName")   val studentName: String,
    @SerializedName("email")         val email: String,
    @SerializedName("academicLevel") val academicLevel: String?,
    @SerializedName("interests")     val interests: String?
)
