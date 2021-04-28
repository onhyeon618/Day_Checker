package com.toyprojects.daychecker.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun StringToLocalDate(value: String?): LocalDate? {
        return LocalDate.parse(value, DateTimeFormatter.ISO_DATE)
    }

    @TypeConverter
    fun LocalDateToString(date: LocalDate?): String? {
        return date?.toString()
    }
}