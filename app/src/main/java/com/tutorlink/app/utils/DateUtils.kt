package com.tutorlink.app.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val esLocale = Locale("es", "CO")

fun String.toReadableDateTime(): String = try {
    val dt = LocalDateTime.parse(this)
    val day   = dt.format(DateTimeFormatter.ofPattern("EEE", esLocale)).replaceFirstChar { it.uppercase() }
    val date  = dt.format(DateTimeFormatter.ofPattern("d MMM", esLocale))
    val time  = dt.format(DateTimeFormatter.ofPattern("hh:mm a", esLocale))
    "$day, $date · $time"
} catch (_: Exception) { this }

fun String.toReadableDate(): String = try {
    LocalDateTime.parse(this)
        .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", esLocale))
} catch (_: Exception) { this }
