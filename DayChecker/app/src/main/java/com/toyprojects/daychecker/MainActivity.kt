package com.toyprojects.daychecker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.gms.ads.MobileAds
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.toyprojects.daychecker.database.Record
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
    private lateinit var roomdb: RecordDB

    private var doubleBackPressed: Boolean = false

    // global variables for setting up CalendarView
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val titleFormatter = DateTimeFormatter.ofPattern("yyyy년 MMM")

    private var getToday = false

    private var numOfRecords = mutableMapOf<LocalDate, Int>()

    // set as global to enable canceling
    private lateinit var closeToast: Toast

    private val rvAdapter = DayRecordAdapter(object : DayRecordAdapter.ItemMenuClickListener {
        override fun onItemMenuClicked(position: Int) {
            val popupMenu = PopupMenu(
                applicationContext,
                binding.recordsRV[position].findViewById(R.id.btnActions)
            )
            popupMenu.inflate(R.menu.recycler_menu)

            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.edit_record -> {
                            openEditor(position)
                            return true
                        }
                        R.id.delete_record -> {
                            deleteConfirmMsg(position)
                            return true
                        }
                    }
                    return false
                }
            })
            popupMenu.show()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        // Return to main theme from splash screen
        setTheme(R.style.ThNoActionBar)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        // Toolbar
        val topAppBar = binding.toolbar
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivityForResult(intent, 4321)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.no_transition)
                    true
                }
                else -> false
            }
        }

        // Add record buttons
        binding.btnNewRecord.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra(EditorState.varName, EditorState.NEW_RECORD)
            intent.putExtra("selectedDate", selectedDate.toString())
            startActivityForResult(intent, EditorState.NEW_RECORD)
            overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
        }
        binding.btnAddRecord.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra(EditorState.varName, EditorState.NEW_RECORD)
            intent.putExtra("selectedDate", selectedDate.toString())
            startActivityForResult(intent, EditorState.NEW_RECORD)
            overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
        }

        // Local (Room) Database
        roomdb = Room.databaseBuilder(
            applicationContext,
            RecordDB::class.java, "dayCheckRecord"
        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

        runBlocking {
            // Read entire database to get <date - num of records> pair
            val itemCounts = roomdb.recordDao().countRecordPerDay()

            // set each pair as map for later use
            for (each in itemCounts) {
                numOfRecords[each.record_date] = each.num_of_records
            }
        }

        // set up calendar
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(60)
        val lastMonth = currentMonth.plusMonths(12)
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
                        0 -> {
                            dotView1.visibility = View.GONE
                            dotView2.visibility = View.GONE
                            dotView3.visibility = View.GONE
                        }
                        1 -> {
                            dotView1.visibility = View.VISIBLE
                            dotView2.visibility = View.GONE
                            dotView3.visibility = View.GONE
                        }
                        2 -> {
                            dotView1.visibility = View.VISIBLE
                            dotView2.visibility = View.VISIBLE
                            dotView3.visibility = View.GONE
                        }
                        else -> {
                            dotView1.visibility = View.VISIBLE
                            dotView2.visibility = View.VISIBLE
                            dotView3.visibility = View.VISIBLE
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
                    textView.setTextColor(Color.BLACK)
                    textView.background = null
                    textView.alpha = 0.3f
                    dotsLayout.alpha = 0.3f
                }
            }
        }

        // When scrolled (or scroll detected)
        binding.calendarView.monthScrollListener = {
            binding.currentMonthText.text = titleFormatter.format(it.yearMonth)

            if (getToday) {
                selectDate(today)
                getToday = false
            }
            else {
                // select first day of month -> must set to arrange recyclerview
                selectDate(it.yearMonth.atDay(1))
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
            getToday = true
            binding.calendarView.smoothScrollToMonth(currentMonth)
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

        // setup RecyclerView
        binding.recordsRV.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        rvAdapter.listData = loadData(today)
        binding.recordsRV.adapter = rvAdapter

        binding.recordsRV.layoutManager = LinearLayoutManager(this)
    }

    // function to get list of records of certain date
    private fun loadData(date: LocalDate): MutableList<Record> {
        var recordList: MutableList<Record>

        runBlocking {
            recordList = roomdb.recordDao().getRecordByDate(date)
        }

        return recordList
    }

    // When user clicked date
    private fun selectDate(date: LocalDate) {
        // keep previous date to change its appearance
        val oldDate = selectedDate

        if (oldDate != date) {
            selectedDate = date

            binding.calendarView.notifyDateChanged(date)
            if (oldDate != null) {
                binding.calendarView.notifyDateChanged(oldDate)
            }

            // if record exist, show recyclerview and its records; else hide
            if (!numOfRecords.containsKey(date)) {
                binding.layoutRecordExist.visibility = View.GONE
                binding.layoutNoRecord.visibility = View.VISIBLE
            }
            else if (numOfRecords[date] == 0) {
                binding.layoutRecordExist.visibility = View.GONE
                binding.layoutNoRecord.visibility = View.VISIBLE
            }
            else {
                binding.layoutRecordExist.visibility = View.VISIBLE
                binding.layoutNoRecord.visibility = View.GONE

                rvAdapter.listData = loadData(date)
                binding.recordsRV.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun openEditor(position: Int) {
        val clickedItem = rvAdapter.listData[position]
        val intent = Intent(applicationContext, EditorActivity::class.java)

        intent.putExtra(EditorState.varName, EditorState.EDIT_RECORD)

        intent.putExtra("recordID", clickedItem.id)
        intent.putExtra("recordDate", clickedItem.record_date.toString())
        intent.putExtra("recordTime", clickedItem.record_time)
        intent.putExtra("recordCondition", clickedItem.condition)
        intent.putExtra("recordState", clickedItem.state)
        intent.putExtra("recordRating", clickedItem.rating)
        intent.putExtra("recordMemo", clickedItem.memo)

        startActivityForResult(intent, EditorState.EDIT_RECORD)
        overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
    }

    private fun deleteConfirmMsg(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.delete_record_confirm))
            .setPositiveButton(getString(R.string.delete_record)) { _, _ ->
                val clickedItem = rvAdapter.listData[position]

                runBlocking {
                    roomdb.recordDao().delete(clickedItem)
                }

                // refresh recyclerview
                rvAdapter.listData.removeAt(position)
                rvAdapter.notifyDataSetChanged()

                // refresh calendarview
                numOfRecords[selectedDate!!] = numOfRecords[selectedDate]!!.minus(1)
                binding.calendarView.notifyDateChanged(selectedDate!!)

                // refresh fragment
                if (numOfRecords[selectedDate] == 0) {
                    binding.layoutNoRecord.visibility = View.VISIBLE
                    binding.layoutRecordExist.visibility = View.GONE
                }
            }
            .setNegativeButton(getString(R.string.cancel_text), null)
            .setCancelable(true)

        builder.create().show()
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
                        numOfRecords[updatedDate] = numOfRecords[updatedDate]!!.plus(1)
                    } else {
                        numOfRecords[updatedDate] = 1
                    }

                    binding.calendarView.notifyDateChanged(updatedDate)

                    // update recyclerview
                    rvAdapter.listData = loadData(updatedDate)
                    binding.recordsRV.adapter?.notifyDataSetChanged()

                    // set visibility of each layout
                    binding.layoutRecordExist.visibility = View.VISIBLE
                    binding.layoutNoRecord.visibility = View.GONE
                }
                EditorState.EDIT_RECORD -> {
                    val updated = data?.getStringExtra("updatedDate")
                    val updatedDate = LocalDate.parse(updated, DateTimeFormatter.ISO_DATE)

                    // check if date changed
                    if (updatedDate != selectedDate) {
                        // minus one record from selectedDate
                        numOfRecords[selectedDate!!] = numOfRecords[selectedDate]!!.minus(1)
                        binding.calendarView.notifyDateChanged(selectedDate!!)

                        // refresh fragment
                        if (numOfRecords[selectedDate] == 0) {
                            binding.layoutNoRecord.visibility = View.VISIBLE
                            binding.layoutRecordExist.visibility = View.GONE
                        }

                        // add one record to updatedDate
                        if (numOfRecords.containsKey(updatedDate)) {
                            numOfRecords[updatedDate] = numOfRecords[updatedDate]!!.plus(1)
                        } else {
                            numOfRecords[updatedDate] = 1
                        }
                        binding.calendarView.notifyDateChanged(updatedDate)
                    }
                    val oldDate = selectedDate
                    selectedDate = null
                    selectDate(oldDate!!)
                }
                4321 -> {
                    val isDataReset = data?.getIntExtra("dataReset", 0)
                    if (isDataReset == DataBackupState.DATA_RESET) {
                        numOfRecords.clear()
                        binding.calendarView.notifyCalendarChanged()

                        binding.layoutRecordExist.visibility = View.GONE
                        binding.layoutNoRecord.visibility = View.VISIBLE
                    }
                    val isDataImported = data?.getIntExtra("dataImport", 0)
                    if (isDataImported == DataBackupState.DATA_IMPORT) {
                        // re-build database
                        roomdb = Room.databaseBuilder(
                            applicationContext,
                            RecordDB::class.java, "dayCheckRecord"
                        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

                        runBlocking {
                            val itemCounts = roomdb.recordDao().countRecordPerDay()

                            for (each in itemCounts) {
                                numOfRecords[each.record_date] = each.num_of_records
                            }
                        }

                        binding.calendarView.notifyCalendarChanged()
                        selectedDate = null   // must set it to call selectDate properly
                        selectDate(today)
                    }
                }
            }
        }
    }

    // BackPress twice to close the app
    override fun onBackPressed() {
        if (doubleBackPressed) {
            closeToast.cancel()
            super.onBackPressed()
            return
        }
        this.doubleBackPressed = true
        closeToast = Toast.makeText(this, getString(R.string.backpress_close), Toast.LENGTH_SHORT)
        closeToast.show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackPressed = false }, 2000)
    }
}