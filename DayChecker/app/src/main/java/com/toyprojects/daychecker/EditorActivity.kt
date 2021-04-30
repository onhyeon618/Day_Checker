package com.toyprojects.daychecker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.toyprojects.daychecker.database.Record
import com.toyprojects.daychecker.database.RecordDB
import com.toyprojects.daychecker.databinding.ActivityRecordEditorBinding
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordEditorBinding
    private lateinit var recordDB: RecordDB

    private var clickable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var recordDate = LocalDate.now()
        var recordTime = String.format("%02d : %02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE))

        recordDB = Room.databaseBuilder(
            this,
            RecordDB::class.java, "dayCheckRecord"
        ).build()

        // Check where is the page called from:
        // if page opened for editing existing record, set its data on corresponding place
        // if writing new record, set date to be selected date, time to be current time
        val calledState = intent.getIntExtra(EditorState.varName, 0)
        if (calledState == EditorState.EDIT_RECORD) {
            // 기존 기록의 내용을 각 위젯에 세팅하는 코드
            // 레코드 RecyclerView 구축 선행 필요
        }
        else {
            intent.getStringExtra("selectedDate")?.let {
                binding.dtvDatePicker.setDTText(it)
                recordDate =  LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
            }
            binding.dtvTimePicker.setDTText(recordTime)
        }

        val topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button)
        topAppBar.setTitle(R.string.records)

        topAppBar.setNavigationOnClickListener {
            if (isChanged()) { showEndMsg() }
            else { finish() }
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_record -> {
                    if (clickable) {
                        // prevent multiple clicks
                        clickable = false

                        var conditionCode = 0
                        var stateCode = 0

                        // get which condition is checked
                        when(binding.rgCondition.checkedRadioButtonId) {
                            binding.rbCondition1.id -> conditionCode = 1
                            binding.rbCondition2.id -> conditionCode = 2
                            binding.rbCondition3.id -> conditionCode = 3
                            binding.rbCondition4.id -> conditionCode = 4
                            binding.rbCondition5.id -> conditionCode = 5
                            binding.rbCondition6.id -> conditionCode = 6
                            binding.rbCondition7.id -> conditionCode = 7
                            binding.rbCondition8.id -> conditionCode = 8
                        }

                        // get which state is checked
                        when(binding.rgState.checkedRadioButtonId) {
                            binding.rgState1.id -> stateCode = 1
                            binding.rgState2.id -> stateCode = 2
                            binding.rgState3.id -> stateCode = 3
                        }

                        // Record instance
                        val userRecord = Record(0, recordDate, recordTime, conditionCode, stateCode,
                                                binding.ratingBar.rating, binding.txtRecordMemo.text.toString().trim())

                        if (calledState == EditorState.EDIT_RECORD) {
                            // set instance id to the one of given data
                            userRecord.id = 1
                            editRecord(userRecord)
                        }
                        else { saveNewRecord(userRecord) }

                        // Tell MainActivity successfully edited
                        val parent = Intent()
                        parent.putExtra("updatedDate", recordDate.toString())
                        setResult(RESULT_OK, parent)

                        finish()
                    }
                    true
                }
                else -> false
            }
        }

        // show DatePicker, get date, set selected date on textview
        binding.dtvDatePicker.setOnClickListener {
            val year = recordDate.year
            val month = recordDate.monthValue
            val day = recordDate.dayOfMonth

            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    binding.dtvDatePicker.setDTText(String.format("%d-%02d-%02d", year, month+1, dayOfMonth))
                    recordDate = LocalDate.parse(String.format("%d-%02d-%02d", year, month+1, dayOfMonth), DateTimeFormatter.ISO_DATE)
                }, year, month-1, day)

            datePicker.show()
        }

        // show TimePicker, get time, set selected time on textview
        binding.dtvTimePicker.setOnClickListener {
            val hour = recordTime.split(" : ")[0].toInt()
            val minute = recordTime.split(" : ")[1].toInt()

            val mTimePicker = TimePickerDialog(this,
                { _, hourOfDay, minute ->
                    recordTime = String.format("%02d : %02d", hourOfDay, minute)
                    binding.dtvTimePicker.setDTText(recordTime)
                }, hour, minute, false)

            mTimePicker.show()
        }
    }

    private fun saveNewRecord(record: Record) = runBlocking {
        recordDB.recordDao().insert(record)
    }

    private fun editRecord(record: Record) = runBlocking {
        recordDB.recordDao().update(record)
    }

    private fun isChanged() : Boolean {
        if (binding.rgCondition.checkedRadioButtonId != binding.rbCondition1.id
            || binding.rgState.checkedRadioButtonId != binding.rgState1.id
            || binding.ratingBar.rating != 0.0F
            || binding.txtRecordMemo.text.toString().trim() != "") {
                return true
        }
        return false
    }

    // show AlertDialog when user tries to cancel it with text written
    private fun showEndMsg() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("변경사항이 있습니다. 변경사항을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ -> finish() }
            .setNegativeButton("취소", null)
            .setCancelable(true)

        builder.create().show()
    }

    override fun onBackPressed() {
        if (isChanged()) { showEndMsg() }
        else { finish() }
    }
}