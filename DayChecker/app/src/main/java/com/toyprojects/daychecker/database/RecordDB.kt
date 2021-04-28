package com.toyprojects.daychecker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Record::class], version = 1)
@TypeConverters(Converters::class)
abstract class RecordDB: RoomDatabase() {
    abstract fun recordDao(): RecordDao
}