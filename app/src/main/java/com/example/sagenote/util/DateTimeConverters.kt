package com.example.sagenote.util

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

/**
 * Type converters for Room to handle kotlinx.datetime.Instant
 */
class DateTimeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}
