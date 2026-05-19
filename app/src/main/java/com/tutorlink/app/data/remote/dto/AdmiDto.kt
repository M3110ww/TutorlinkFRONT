package com.tutorlink.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * FIXED: Backend returns Map<String,Long>. Gson deserializes JSON numbers
 * as Double when target type is Any/Object. Using Double explicitly and
 * converting to Long in the UI avoids ClassCastException.
 */
data class AdminStatsResponse(
    @SerializedName("PENDING")   val pending: Double = 0.0,
    @SerializedName("CONFIRMED") val confirmed: Double = 0.0,
    @SerializedName("CANCELLED") val cancelled: Double = 0.0,
    @SerializedName("COMPLETED") val completed: Double = 0.0
) {
    val pendingLong   get() = pending.toLong()
    val confirmedLong get() = confirmed.toLong()
    val cancelledLong get() = cancelled.toLong()
    val completedLong get() = completed.toLong()
}
