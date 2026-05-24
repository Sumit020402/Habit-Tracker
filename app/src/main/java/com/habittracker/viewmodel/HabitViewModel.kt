package com.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.habittracker.data.Habit
import com.habittracker.data.HabitDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = HabitDatabase.getDatabase(application).habitDao()
    val allHabits: LiveData<List<Habit>> = dao.getAllHabits()

    fun addHabit(habit: Habit) = viewModelScope.launch {
        dao.insertHabit(habit)
    }

    fun updateHabit(habit: Habit) = viewModelScope.launch {
        dao.updateHabit(habit)
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        dao.deleteHabit(habit)
    }

    fun toggleComplete(habit: Habit) = viewModelScope.launch {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        if (!habit.isCompletedToday) {
            // ✅ CHECKING — mark complete, increase streak
            val newStreak = when (habit.lastCompletedDate) {
                yesterday -> habit.streak + 1  // continued streak
                today     -> habit.streak       // already done (safety)
                else      -> 1                  // streak broken, restart
            }

            val history = if (habit.completionHistory.isEmpty()) today
            else "${habit.completionHistory},$today"

            dao.updateHabit(habit.copy(
                isCompletedToday = true,
                streak = newStreak,
                lastCompletedDate = today,
                completionHistory = history
            ))
        } else {
            // ❌ UNCHECKING — revert today's completion
            val history = habit.completionHistory
                .split(",")
                .filter { it.isNotEmpty() && it != today }
                .joinToString(",")

            // Recalculate streak after removing today
            val newStreak = when {
                habit.lastCompletedDate != today -> habit.streak // wasn't today
                history.isEmpty()                -> 0
                else -> {
                    // Check if yesterday was completed to restore streak
                    val dates = history.split(",").filter { it.isNotEmpty() }
                    val lastDate = dates.lastOrNull() ?: ""
                    if (lastDate == yesterday) habit.streak - 1 else 0
                }
            }

            dao.updateHabit(habit.copy(
                isCompletedToday = false,
                streak = newStreak.coerceAtLeast(0),
                lastCompletedDate = history.split(",")
                    .filter { it.isNotEmpty() }.lastOrNull() ?: "",
                completionHistory = history
            ))
        }
    }
}