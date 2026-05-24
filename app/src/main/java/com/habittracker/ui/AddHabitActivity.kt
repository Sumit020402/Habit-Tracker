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
import com.habittracker.viewmodel.HabitViewModel
import java.util.Calendar

class AddHabitActivity : AppCompatActivity() {

    private val viewModel: HabitViewModel by viewModels()
    private var selectedTime = ""
    private var editingHabit: Habit? = null  // null = adding, non-null = editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        // Check if editing an existing habit
        editingHabit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("habit", Habit::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("habit")
        }

        supportActionBar?.apply {
            title = if (editingHabit != null) "Edit Habit" else "Add New Habit"
            setDisplayHomeAsUpEnabled(true)
        }

        // Pre-fill fields if editing
        editingHabit?.let { habit ->
            findViewById<TextInputEditText>(R.id.etHabitName).setText(habit.name)
            findViewById<TextInputEditText>(R.id.etDescription).setText(habit.description)
            if (habit.reminderTime.isNotEmpty()) {
                selectedTime = habit.reminderTime
                findViewById<Switch>(R.id.switchReminder).isChecked = true
                findViewById<TextView>(R.id.tvSelectedTime).text =
                    "⏰ Reminder set for ${habit.reminderTime}"
            }
        }

        setupTimePicker()
        setupSaveButton()
    }

    private fun setupTimePicker() {
        val switchReminder = findViewById<Switch>(R.id.switchReminder)
        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
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
            },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupSaveButton() {
        val etName = findViewById<TextInputEditText>(R.id.etHabitName)
        val etDesc = findViewById<TextInputEditText>(R.id.etDescription)
        val switchReminder = findViewById<Switch>(R.id.switchReminder)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Change button label if editing
        btnSave.text = if (editingHabit != null) "Update Habit" else "Save Habit"

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val description = etDesc.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Please enter a habit name"
                return@setOnClickListener
            }

            val reminderTime = if (switchReminder.isChecked && selectedTime.isNotEmpty())
                selectedTime else ""

            val habit = editingHabit?.copy(
                name = name,
                description = description,
                reminderTime = reminderTime
            ) ?: Habit(
                name = name,
                description = description,
                reminderTime = reminderTime
            )

            if (editingHabit != null) {
                viewModel.updateHabit(habit)
                Toast.makeText(this, "✅ Habit updated!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addHabit(habit)
                Toast.makeText(this, "✅ Habit saved!", Toast.LENGTH_SHORT).show()
            }

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
            this,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Use setExactAndAllowWhileIdle for reliable delivery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}