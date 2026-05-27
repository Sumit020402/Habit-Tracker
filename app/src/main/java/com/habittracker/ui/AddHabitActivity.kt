package com.habittracker.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.habittracker.R
import com.habittracker.data.Habit
import com.habittracker.notifications.ReminderReceiver
import com.habittracker.utils.DayUtils
import com.habittracker.viewmodel.HabitViewModel
import java.util.Calendar

class AddHabitActivity : AppCompatActivity() {

    private val viewModel: HabitViewModel by viewModels()
    private var selectedTime = ""
    private var editingHabit: Habit? = null

    // Track selected days (1=Mon...7=Sun)
    private val selectedDays = mutableSetOf(1, 2, 3, 4, 5) // Mon-Fri default

    // Day chip views in order Mon..Sun
    private lateinit var dayChips: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        editingHabit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("habit", Habit::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra("habit")
        }

        supportActionBar?.apply {
            title = if (editingHabit != null) "Edit Habit" else "Add New Habit"
            setDisplayHomeAsUpEnabled(true)
        }

        dayChips = listOf(
            findViewById(R.id.chipMon), findViewById(R.id.chipTue),
            findViewById(R.id.chipWed), findViewById(R.id.chipThu),
            findViewById(R.id.chipFri), findViewById(R.id.chipSat),
            findViewById(R.id.chipSun)
        )

        // Pre-fill if editing
        editingHabit?.let { habit ->
            findViewById<TextInputEditText>(R.id.etHabitName).setText(habit.name)
            findViewById<TextInputEditText>(R.id.etDescription).setText(habit.description)
            if (habit.reminderTime.isNotEmpty()) {
                selectedTime = habit.reminderTime
                findViewById<Switch>(R.id.switchReminder).isChecked = true
                findViewById<TextView>(R.id.tvSelectedTime).text =
                    "⏰ Reminder set for ${habit.reminderTime}"
            }
            // Load saved days
            selectedDays.clear()
            selectedDays.addAll(DayUtils.parseDays(habit.activeDays))

            // Load goal
            if (habit.dailyGoal.isNotEmpty()) {
                findViewById<TextInputEditText>(R.id.etGoalAmount).setText(habit.dailyGoal)
                findViewById<TextInputEditText>(R.id.etGoalUnit).setText(habit.goalUnit)
            }
        }

        setupDayChips()
        setupTimePicker()
        setupSaveButton()
    }

    private fun setupDayChips() {
        dayChips.forEachIndexed { index, chip ->
            val dayNum = index + 1
            updateChipStyle(chip, dayNum in selectedDays)
            chip.setOnClickListener {
                if (dayNum in selectedDays) {
                    if (selectedDays.size > 1) { // keep at least 1 day
                        selectedDays.remove(dayNum)
                        updateChipStyle(chip, false)
                    } else {
                        Toast.makeText(this,
                            "At least one day must be selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedDays.add(dayNum)
                    updateChipStyle(chip, true)
                }
            }
        }
    }

    private fun updateChipStyle(chip: TextView, isActive: Boolean) {
        chip.background = getDrawable(
            if (isActive) R.drawable.bg_day_chip_active
            else R.drawable.bg_day_chip_inactive
        )
        chip.setTextColor(getColor(
            if (isActive) android.R.color.white
            else android.R.color.darker_gray
        ))
    }

    private fun setupTimePicker() {
        val switchReminder = findViewById<Switch>(R.id.switchReminder)
        val btnPickTime    = findViewById<Button>(R.id.btnPickTime)
        val tvSelectedTime = findViewById<TextView>(R.id.tvSelectedTime)

        btnPickTime.setOnClickListener {
            if (!switchReminder.isChecked) {
                Toast.makeText(this, "Enable reminder first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                tvSelectedTime.text = "⏰ Reminder set for $selectedTime"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun setupSaveButton() {
        val etName       = findViewById<TextInputEditText>(R.id.etHabitName)
        val etDesc       = findViewById<TextInputEditText>(R.id.etDescription)
        val etGoalAmount = findViewById<TextInputEditText>(R.id.etGoalAmount)
        val etGoalUnit   = findViewById<TextInputEditText>(R.id.etGoalUnit)
        val switchReminder = findViewById<Switch>(R.id.switchReminder)
        val btnSave      = findViewById<Button>(R.id.btnSave)

        btnSave.text = if (editingHabit != null) "Update Habit" else "Save Habit"

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Please enter a habit name"
                return@setOnClickListener
            }

            val reminderTime = if (switchReminder.isChecked && selectedTime.isNotEmpty())
                selectedTime else ""
            val goalAmount = etGoalAmount.text.toString().trim()
            val goalUnit   = etGoalUnit.text.toString().trim()

            val habit = editingHabit?.copy(
                name = name,
                description = etDesc.text.toString().trim(),
                reminderTime = reminderTime,
                activeDays = DayUtils.encodeDays(selectedDays),
                dailyGoal = goalAmount,
                goalUnit = goalUnit
            ) ?: Habit(
                name = name,
                description = etDesc.text.toString().trim(),
                reminderTime = reminderTime,
                activeDays = DayUtils.encodeDays(selectedDays),
                dailyGoal = goalAmount,
                goalUnit = goalUnit
            )

            if (editingHabit != null) viewModel.updateHabit(habit)
            else viewModel.addHabit(habit)

            Toast.makeText(this,
                if (editingHabit != null) "✅ Habit updated!" else "✅ Habit saved!",
                Toast.LENGTH_SHORT).show()

            if (reminderTime.isNotEmpty()) scheduleReminder(habit)
            finish()
        }
    }

    private fun scheduleReminder(habit: Habit) {
        val timeParts = habit.reminderTime.split(":")
        if (timeParts.size != 2) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("habit_name", habit.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, habit.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis, pendingIntent)
            } else {
                setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}