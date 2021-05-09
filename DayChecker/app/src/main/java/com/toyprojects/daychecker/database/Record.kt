package com.toyprojects.daychecker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

private const val TABLE_NAME = "dayCheckRecord"

@Entity(
    tableName = TABLE_NAME,
    indices = [Index(value = ["record_date"]), Index(value = ["record_time"])]
)
data class Record(
    @PrimaryKey(autoGenerate = true) var id : Int,
    @ColumnInfo(name = "record_date") val record_date : LocalDate,   // will be converted to string
    @ColumnInfo(name = "record_time") val record_time : String,
    @ColumnInfo(name = "condition") val condition : Int,
    @ColumnInfo(name = "state") val state : Int,
    @ColumnInfo(name = "rating") val rating : Float,
    @ColumnInfo(name = "memo") val memo : String
)

data class CountPerDay(
    @ColumnInfo(name = "record_date") val record_date : LocalDate,
    @ColumnInfo(name = "num_of_records") val num_of_records : Int
)