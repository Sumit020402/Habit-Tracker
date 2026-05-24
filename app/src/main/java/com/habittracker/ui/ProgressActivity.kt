package com.habittracker.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.habittracker.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        supportActionBar?.apply {
            title = "Progress"
            setDisplayHomeAsUpEnabled(true)
        }

        // Get data passed from MainActivity
        val habitName = intent.getStringExtra("habit_name") ?: "All Habits"
        val streak = intent.getIntExtra("streak", 0)
        val history = intent.getStringExtra("history") ?: ""
        val total = intent.getIntExtra("total", 0)

        // Set text fields
        findViewById<TextView>(R.id.tvProgressTitle).text = "📊 Progress"
        findViewById<TextView>(R.id.tvHabitNameProgress).text = habitName
        findViewById<TextView>(R.id.tvStreakCount).text = streak.toString()
        findViewById<TextView>(R.id.tvTotalCount).text = total.toString()

        setupBarChart(history)
    }

    private fun setupBarChart(history: String) {
        val chart = findViewById<BarChart>(R.id.barChart)

        // Get last 7 days
        val last7Days = (6 downTo 0).map {
            LocalDate.now().minusDays(it.toLong())
        }

        val completedDates = history.split(",").filter { it.isNotEmpty() }.toSet()

        // Build entries: 1 if completed that day, 0 if not
        val entries = last7Days.mapIndexed { index, date ->
            val dateStr = date.toString()
            val value = if (completedDates.contains(dateStr)) 1f else 0f
            BarEntry(index.toFloat(), value)
        }

        // Day labels (Mon, Tue, etc.)
        val labels = last7Days.map {
            it.format(DateTimeFormatter.ofPattern("EEE"))
        }

        // Style the bars
        val dataSet = BarDataSet(entries, "Completed").apply {
            colors = entries.map { entry ->
                if (entry.y == 1f) Color.parseColor("#6200EE")
                else Color.parseColor("#E0E0E0")
            }
            setDrawValues(false)
        }

        // Configure chart
        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            animateY(800)

            // X Axis
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
                textSize = 12f
            }

            // Y Axis
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 1.5f
                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)
            }
            axisRight.isEnabled = false

            invalidate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}