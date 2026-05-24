package com.habittracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.habittracker.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var tvMonthTitle: TextView
    private lateinit var tvHabitName: TextView
    private lateinit var tvStats: TextView

    private var completedDates = setOf<LocalDate>()
    private val today = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        supportActionBar?.apply {
            title = "Habit History"
            setDisplayHomeAsUpEnabled(true)
        }

        // Get data from intent
        val habitName = intent.getStringExtra("habit_name") ?: "Habit"
        val history   = intent.getStringExtra("history") ?: ""
        val streak    = intent.getIntExtra("streak", 0)

        // Parse completed dates
        completedDates = history
            .split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toSet()

        // Bind views
        calendarView  = findViewById(R.id.calendarView)
        tvMonthTitle  = findViewById(R.id.tvMonthTitle)
        tvHabitName   = findViewById(R.id.tvHabitName)
        tvStats       = findViewById(R.id.tvStats)

        tvHabitName.text = habitName
        tvStats.text     = buildStatsText(streak)

        setupCalendar()
        setupDayLabels()
    }

    private fun buildStatsText(streak: Int): String {
        val total = completedDates.size
        val thisMonth = completedDates.count {
            it.month == today.month && it.year == today.year
        }
        return "🔥 $streak day streak   ✅ $total total   📅 $thisMonth this month"
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth   = currentMonth.minusMonths(12)
        val endMonth     = currentMonth.plusMonths(1)
        val daysOfWeek   = daysOfWeek()

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        // Day binder
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.bind(data, completedDates, today)
            }
        }

        // Month header binder
        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthHeaderContainer> {
                override fun create(view: View) = MonthHeaderContainer(view)
                override fun bind(container: MonthHeaderContainer, data: CalendarMonth) {
                    container.title.text = data.yearMonth
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                }
            }

        // Update top title when scrolled
        calendarView.monthScrollListener = { month ->
            tvMonthTitle.text = month.yearMonth
                .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
    }

    private fun setupDayLabels() {
        val daysOfWeek = daysOfWeek()
        val labels = listOf<TextView>(
            findViewById(R.id.tvSun),
            findViewById(R.id.tvMon),
            findViewById(R.id.tvTue),
            findViewById(R.id.tvWed),
            findViewById(R.id.tvThu),
            findViewById(R.id.tvFri),
            findViewById(R.id.tvSat)
        )
        labels.forEachIndexed { index, tv ->
            tv.text = daysOfWeek[index]
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .uppercase()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// ─── ViewContainers ───────────────────────────────────────────────

class DayViewContainer(view: View) : ViewContainer(view) {
    val tvDay: TextView     = view.findViewById(R.id.tvDay)
    val dotView: View       = view.findViewById(R.id.dotView)
    val dayLayout: View     = view.findViewById(R.id.dayLayout)

    fun bind(day: CalendarDay, completedDates: Set<LocalDate>, today: LocalDate) {
        tvDay.text = day.date.dayOfMonth.toString()

        val isCompleted  = day.date in completedDates
        val isToday      = day.date == today
        val isCurrentMonth = day.position == DayPosition.MonthDate
        val isFuture     = day.date.isAfter(today)

        // Reset styles
        dayLayout.setBackgroundResource(0)
        tvDay.alpha = 1f
        dotView.visibility = View.GONE
        tvDay.setTextColor(
            view.context.getColor(android.R.color.black)
        )

        when {
            !isCurrentMonth -> {
                // Faded out-of-month days
                tvDay.alpha = 0.2f
            }
            isFuture -> {
                // Future days — greyed out
                tvDay.setTextColor(
                    view.context.getColor(android.R.color.darker_gray)
                )
            }
            isCompleted && isToday -> {
                // Completed today — filled circle + dot
                dayLayout.setBackgroundResource(R.drawable.bg_day_completed)
                tvDay.setTextColor(
                    view.context.getColor(android.R.color.white)
                )

            }
            isCompleted -> {
                // Completed past day — filled purple circle
                dayLayout.setBackgroundResource(R.drawable.bg_day_completed)
                tvDay.setTextColor(
                    view.context.getColor(android.R.color.white)
                )
            }
            isToday -> {
                // Today not completed — outlined circle
                dayLayout.setBackgroundResource(R.drawable.bg_day_today)
                tvDay.setTextColor(
                    view.context.getColor(R.color.primary)
                )
            }
            else -> {
                // Missed day — red dot indicator
                dotView.visibility = View.VISIBLE
                dotView.setBackgroundResource(R.drawable.bg_dot_missed)
            }
        }
    }
}

class MonthHeaderContainer(view: View) : ViewContainer(view) {
    val title: TextView = view.findViewById(R.id.tvMonthHeader)
}