package com.focusflow.ai.data.local

import androidx.room.TypeConverter

/**
 * Room type converters. Tag lists are serialised with an ASCII unit-separator
 * so no JSON dependency is required and no escaping rules are needed.
 */
class Converters {

    @TypeConverter
    fun fromTagList(tags: List<String>): String = tags.joinToString(SEPARATOR)

    @TypeConverter
    fun toTagList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR).filter { it.isNotEmpty() }

    companion object {
        private const val SEPARATOR = "\u001F"
    }
}
