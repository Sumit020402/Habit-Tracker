package com.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.habittracker.data.Habit
import com.habittracker.data.HabitDatabase
import com.habittracker.utils.DayUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = HabitDatabase.getDatabase(application).habitDao()
    val allHabits: LiveData<List<Habit>> = dao.getAllHabits()

    // ✅ Called once on app start from MainActivity
    fun resetIfNewDay() = viewModelScope.launch {
        val today = LocalDate.now().toString()
        val habits = dao.getAllHabitsSync() // fetch all habits once
        habits.forEach { habit ->
            // If last completed date is NOT today but isCompletedToday is true → reset
            if (habit.isCompletedToday && habit.lastCompletedDate != today) {
                dao.updateHabit(habit.copy(isCompletedToday = false))
            }
        }
    }

    fun addHabit(habit: Habit) = viewModelScope.launch { dao.insertHabit(habit) }

    fun updateHabit(habit: Habit) = viewModelScope.launch { dao.updateHabit(habit) }

    fun deleteHabit(habit: Habit) = viewModelScope.launch { dao.deleteHabit(habit) }

    // ✅ Fixed toggle — streak correctly recalculates on recheck
    fun toggleComplete(habit: Habit) = viewModelScope.launch {
        val today     = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val hasGoal   = habit.dailyGoal.isNotEmpty()

        if (!habit.isCompletedToday) {
            // ── CHECKING ──────────────────────────────────────────
            val newStreak = when {
                habit.lastCompletedDate == yesterday -> habit.streak + 1
                habit.lastCompletedDate == today     -> habit.streak  // safety
                else                                 -> 1             // restart
            }

            val history = when {
                habit.completionHistory.isEmpty()          -> today
                habit.completionHistory.endsWith(today)    -> habit.completionHistory
                else -> "${habit.completionHistory},$today"
            }

            val newProgress = if (hasGoal)
                DayUtils.setTodayProgress(habit.dailyProgress,
                    habit.dailyGoal.toIntOrNull() ?: 1)
            else habit.dailyProgress

            dao.updateHabit(habit.copy(
                isCompletedToday  = true,
                streak            = newStreak,
                lastCompletedDate = today,
                completionHistory = history,
                dailyProgress     = newProgress
            ))

        } else {
            // ── UNCHECKING ────────────────────────────────────────
            val historyDates = habit.completionHistory
                .split(",")
                .filter { it.isNotEmpty() && it != today }

            // Recalculate what the streak SHOULD be after removing today
            val newStreak = when {
                historyDates.isEmpty()                    -> 0
                historyDates.last() == yesterday          -> habit.streak - 1
                else                                      -> 0
            }.coerceAtLeast(0)

            // lastCompletedDate goes back to the previous completion
            val newLastDate = historyDates.lastOrNull() ?: ""

            val newProgress = if (hasGoal)
                DayUtils.setTodayProgress(habit.dailyProgress, 0)
            else habit.dailyProgress

            dao.updateHabit(habit.copy(
                isCompletedToday  = false,
                streak            = newStreak,
                lastCompletedDate = newLastDate,
                completionHistory = historyDates.joinToString(","),
                dailyProgress     = newProgress
            ))
        }
    }

    // ✅ Fixed increment — uses same streak logic as toggleComplete
    fun incrementProgress(habit: Habit) = viewModelScope.launch {
        val goal    = habit.dailyGoal.toIntOrNull() ?: return@launch
        val today   = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val current = DayUtils.getTodayProgress(habit.dailyProgress)
        val newValue = (current + 1).coerceAtMost(goal)
        val newProgress = DayUtils.setTodayProgress(habit.dailyProgress, newValue)
        val isNowComplete = newValue >= goal

        val newStreak = if (isNowComplete && !habit.isCompletedToday) {
            when {
                habit.lastCompletedDate == yesterday -> habit.streak + 1
                habit.lastCompletedDate == today     -> habit.streak
                else                                 -> 1
            }
        } else habit.streak

        val newHistory = if (isNowComplete && !habit.isCompletedToday) {
            when {
                habit.completionHistory.isEmpty()       -> today
                habit.completionHistory.endsWith(today) -> habit.completionHistory
                else -> "${habit.completionHistory},$today"
            }
        } else habit.completionHistory

        dao.updateHabit(habit.copy(
            dailyProgress     = newProgress,
            isCompletedToday  = isNowComplete,
            streak            = newStreak,
            lastCompletedDate = if (isNowComplete) today else habit.lastCompletedDate,
            completionHistory = newHistory
        ))
    }

    fun resetTodayProgress(habit: Habit) = viewModelScope.launch {
        val newProgress = DayUtils.setTodayProgress(habit.dailyProgress, 0)
        dao.updateHabit(habit.copy(
            dailyProgress    = newProgress,
            isCompletedToday = false
        ))
    }
}