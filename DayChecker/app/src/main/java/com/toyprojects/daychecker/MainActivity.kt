package com.toyprojects.daychecker

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.room.Room
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.toyprojects.daychecker.database.RecordDB
import com.toyprojects.daychecker.databinding.ActivityMainBinding
import com.toyprojects.daychecker.databinding.CalendarDayLayoutBinding
import com.toyprojects.daychecker.databinding.CalendarMonthHeaderLayoutBinding
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var doubleBackPressed: Boolean = false

    // variable for handling app lock state
    private var lock = true

    // global variables for setting up CalendarView
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val titleFormatter = DateTimeFormatter.ofPattern("yyyy년 MMM")

    private var numOfRecords = mutableMapOf<LocalDate, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        var topAppBar = binding.toolbar
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Add record buttons
        binding.btnNewRecord.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra(AppLockState.varName, EditorState.NEW_RECORD)
            intent.putExtra("selectedDate", selectedDate.toString())
            startActivityForResult(intent, EditorState.NEW_RECORD)
        }
        binding.btnAddRecord.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra(AppLockState.varName, EditorState.NEW_RECORD)
            intent.putExtra("selectedDate", selectedDate.toString())
            startActivityForResult(intent, EditorState.NEW_RECORD)
        }

        // Local (Room) Database
        val roomdb = Room.databaseBuilder(
                applicationContext,
                RecordDB::class.java, "dayCheckRecord"
            ).build()

        runBlocking {
            // Read entire database to get <date - num of records> pair
            // 그 달 것만 먼저 가져오도록 하는 편이 효율적이려나... 고민 필요.
            val itemCounts = roomdb.recordDao().countRecordPerDay()

            // set each pair as map for later use
            for (each in itemCounts) {
                numOfRecords[each.record_date] = each.num_of_records
            }
        }

        // set up calendar
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val daysOfWeek = arrayOf("일", "월", "화", "수", "목", "금", "토") // for header
        binding.calendarView.setup(firstMonth, lastMonth, DayOfWeek.SUNDAY)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.post {
            // Show today initially
            selectDate(today)
        }

        // View holder for each date cell -> "calendar_day_layout.xml"
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val dayBinding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.dayBinding.dayText
                val dotsLayout = container.dayBinding.dotsLayout

                val dotView1 = container.dayBinding.dotView1
                val dotView2 = container.dayBinding.dotView2
                val dotView3 = container.dayBinding.dotView3

                textView.text = day.date.dayOfMonth.toString()
                
                // Check if record exists for the day; set dotViews visibility accordingly
                if (numOfRecords.containsKey(day.date)) {
                    when (numOfRecords[day.date]) {
                        1 -> dotView1.isVisible = true
                        2 -> {
                            dotView1.isVisible = true
                            dotView2.isVisible = true
                        }
                        else -> {
                            dotView1.isVisible = true
                            dotView2.isVisible = true
                            dotView3.isVisible = true
                        }
                    }
                }
                else {
                    dotView1.visibility = View.GONE
                    dotView2.visibility = View.GONE
                    dotView3.visibility = View.GONE
                }

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.alpha = 1f
                    if (day.date == selectedDate) {
                        // If this is the selected date...
                        textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.day_selected_bg)
                    } else {
                        // If not
                        textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else {
                    // if dates are not on this month
                    textView.alpha = 0.3f
                    dotsLayout.alpha = 0.3f
                }
            }
        }

        // When scrolled
        binding.calendarView.monthScrollListener = {
            binding.currentMonthText.text = titleFormatter.format(it.yearMonth)

            selectedDate?.let {
                // Clear selection
                selectedDate = null
                binding.calendarView.notifyDateChanged(it)
            }
        }

        // Calendar header(week legend) -> calendar_month_header_layout.xml
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarMonthHeaderLayoutBinding.bind(view).legendLayout
        }
        binding.calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index]
                    }
                }
            }
        }

        // Go back to today's date when logo clicked
        binding.logo.setOnClickListener {
            binding.calendarView.scrollToMonth(currentMonth)
            // smoothScrollToMonth을 쓰면 아래의 selectDate()가 제대로 먹지 않는다. 확인 필요.
            // binding.calendarView.smoothScrollToMonth(currentMonth)
            binding.calendarView.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        // prev/next month buttons
        binding.previousMonthImage.setOnClickListener{
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previous)
            }
        }
        binding.nextMonthImage.setOnClickListener{
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.next)
            }
        }
    }

    // When user clicked date
    private fun selectDate(date: LocalDate) {
        // keep previous date to change its appearance
        val oldDate = selectedDate

        if (oldDate != date) {
            selectedDate = date
            binding.calendarView.notifyDateChanged(date)
            oldDate?.let {
                binding.calendarView.notifyDateChanged(oldDate)
            }
        }

        // 선택한 날짜의 기록 여부에 따라...
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK) {
            when(requestCode) {
                EditorState.NEW_RECORD -> {
                    val updated = data?.getStringExtra("updatedDate")
                    val updatedDate = LocalDate.parse(updated, DateTimeFormatter.ISO_DATE)

                    // change array value to show dots accordingly
                    if (numOfRecords.containsKey(updatedDate)) {
                        numOfRecords[LocalDate.parse(updated, DateTimeFormatter.ISO_DATE)] =
                            numOfRecords[LocalDate.parse(updated, DateTimeFormatter.ISO_DATE)]!!.plus(1)
                    }
                    else {
                        numOfRecords[LocalDate.parse(updated, DateTimeFormatter.ISO_DATE)] = 1
                    }

                    binding.calendarView.notifyDateChanged(updatedDate)
                }
            }
        }
    }

    // BackPress twice to close the app
    override fun onBackPressed() {
        if (doubleBackPressed) {
            super.onBackPressed()
            return
        }
        this.doubleBackPressed = true
        Toast.makeText(this, "\'뒤로\' 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackPressed = false }, 2000)
    }
}