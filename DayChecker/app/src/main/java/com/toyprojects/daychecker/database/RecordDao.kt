package com.toyprojects.daychecker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate

@Dao
interface RecordDao {
    // Number of Records per date
    @Query("SELECT record_date, count(*) AS num_of_records " +
            "FROM dayCheckRecord " +
            "GROUP BY record_date")
    suspend fun countRecordPerDay(): List<CountPerDay>

    // Get Records written in selected date
    @Query("SELECT * FROM dayCheckRecord WHERE record_date = :date")
    suspend fun getRecordByDate(date: LocalDate): List<Record>   // "suspend" for Coroutine

    @Insert
    suspend fun insert(records: Record)

    @Delete
    suspend fun delete(records: Record)

    @Query("DELETE FROM dayCheckRecord")
    suspend fun deleteAll()
}