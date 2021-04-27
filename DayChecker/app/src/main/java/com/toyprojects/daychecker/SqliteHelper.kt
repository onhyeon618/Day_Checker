package com.toyprojects.daychecker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class SqliteHelper(context: Context, name: String, version: Int):
    SQLiteOpenHelper(context, name, null, version) {

    private val ENTIRE_RECORD = "dayCheckRecord"

    override fun onCreate(db: SQLiteDatabase?) {
        val CRETE_SQL = "CREATE_TABLE" + ENTIRE_RECORD + "(" +
                "id INTEGER PRIMARY KEY, " +
                "record_date INTEGER, " +
                "record_time INTEGER, " +
                "condition INTEGER, " +
                "state INTEGER, " +
                "rating INTEGER, " +
                "memo TEXT DEFAULT ''" + ")"

        val CREATE_INDEX_SQL = "CREATE INDEX " + ENTIRE_RECORD + "_IDX ON " + ENTIRE_RECORD + "(" + "record_date" + ")"

        db?.execSQL(CRETE_SQL)
        db?.execSQL(CREATE_INDEX_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun insertRecord(record: DayCheckRecord) {
        val values = ContentValues()
        values.put("record_date", record.record_date)
        values.put("record_time", record.record_time)
        values.put("condition", record.condition)
        values.put("state", record.state)
        values.put("rating", record.rating)
        values.put("memo", record.memo)

        val writer = writableDatabase
        writer.insert(ENTIRE_RECORD, null, values)
        writer.close()
    }

    fun selectRecord(date: Date): MutableList<DayCheckRecord> {
        val list = mutableListOf<DayCheckRecord>()

        val SELECT_SQL = "SELECT *" +
                "FROM" + ENTIRE_RECORD +
                "WHERE record_date = " + date

        val reader = readableDatabase
        val cursor = reader.rawQuery(SELECT_SQL, null)

        while(cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex("id"))
            val record_date = cursor.getLong(cursor.getColumnIndex("record_date"))
            val record_time = cursor.getLong(cursor.getColumnIndex("record_time"))
            val condition = cursor.getInt(cursor.getColumnIndex("condition"))
            val state = cursor.getInt(cursor.getColumnIndex("state"))
            val rating = cursor.getFloat(cursor.getColumnIndex("rating"))
            val memo = cursor.getString(cursor.getColumnIndex("memo"))

            list.add(DayCheckRecord(id, record_date, record_time, condition, state, rating, memo))
        }

        cursor.close()
        reader.close()

        return list
    }

    fun updateRecord(record: DayCheckRecord) {
        val values = ContentValues()
        values.put("record_date", record.record_date)
        values.put("record_time", record.record_time)
        values.put("condition", record.condition)
        values.put("state", record.state)
        values.put("rating", record.rating)
        values.put("memo", record.memo)

        val writer = writableDatabase
        writer.update(ENTIRE_RECORD, values, "id = ?", arrayOf("${record.id}"))
        writer.close()
    }

    fun deleteRecord(record: DayCheckRecord) {
        val DELETE_SQL = "DELETE" +
                "FROM" + ENTIRE_RECORD +
                "WHERE id = ${record.id}"

        val writer = writableDatabase
        writer.execSQL(DELETE_SQL)
        writer.close()
    }
}

data class DayCheckRecord(
    var id: Long?, var record_date: Long, var record_time: Long,
    var condition: Int, var state: Int, var rating: Float, var memo: String?
)