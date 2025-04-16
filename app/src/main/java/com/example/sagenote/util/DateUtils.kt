package com.example.sagenote.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats an Instant to a human-readable date string
 */
fun formatDate(instant: Instant): String {
    val date = Date(instant.toEpochMilliseconds())
    val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    return formatter.format(date)
}
