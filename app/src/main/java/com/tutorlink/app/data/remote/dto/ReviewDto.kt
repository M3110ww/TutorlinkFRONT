package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("rating")  val rating: Int,
    @SerializedName("comment") val comment: String?
)

/**
 * FIXED: The backend returns the Resena entity directly (not a DTO).
 * The entity has nested objects: session{...} and student{...}.
 * We map only the flat fields we need, ignoring nested objects
 * via @SerializedName — Gson will skip unmapped fields silently.
 */
data class ReviewResponse(
    @SerializedName("id")        val id: Long,
    @SerializedName("rating")    val rating: Int,
    @SerializedName("comment")   val comment: String?,
    @SerializedName("createdAt") val createdAt: String?
    // session and student are nested objects in the real response
    // but we don't need them here — Gson ignores extra fields by default
)