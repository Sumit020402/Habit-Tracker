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

    fun addHabit(habit: Habit) = viewModelScope.launch { dao.insertHabit(habit) }

    fun updateHabit(habit: Habit) = viewModelScope.launch { dao.updateHabit(habit) }

    fun deleteHabit(habit: Habit) = viewModelScope.launch { dao.deleteHabit(habit) }

    // ✅ Toggle complete (respects active days)
    fun toggleComplete(habit: Habit) = viewModelScope.launch {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        // If habit has a goal, toggling marks it fully complete
        val hasGoal = habit.dailyGoal.isNotEmpty()

        if (!habit.isCompletedToday) {
            val newStreak = when (habit.lastCompletedDate) {
                yesterday -> habit.streak + 1
                today     -> habit.streak
                else      -> 1
            }
            val history = if (habit.completionHistory.isEmpty()) today
            else "${habit.completionHistory},$today"

            // If has goal, set progress to max
            val newProgress = if (hasGoal)
                DayUtils.setTodayProgress(habit.dailyProgress,
                    habit.dailyGoal.toIntOrNull() ?: 1)
            else habit.dailyProgress

            dao.updateHabit(habit.copy(
                isCompletedToday = true,
                streak = newStreak,
                lastCompletedDate = today,
                completionHistory = history,
                dailyProgress = newProgress
            ))
        } else {
            val history = habit.completionHistory
                .split(",").filter { it.isNotEmpty() && it != today }
                .joinToString(",")

            val newStreak = when {
                habit.lastCompletedDate != today -> habit.streak
                history.isEmpty()                -> 0
                else -> {
                    val dates = history.split(",").filter { it.isNotEmpty() }
                    if (dates.lastOrNull() == yesterday) habit.streak - 1 else 0
                }
            }

            val newProgress = if (hasGoal)
                DayUtils.setTodayProgress(habit.dailyProgress, 0)
            else habit.dailyProgress

            dao.updateHabit(habit.copy(
                isCompletedToday = false,
                streak = newStreak.coerceAtLeast(0),
                lastCompletedDate = history.split(",")
                    .filter { it.isNotEmpty() }.lastOrNull() ?: "",
                completionHistory = history,
                dailyProgress = newProgress
            ))
        }
    }

    // ✅ Increment goal progress (e.g. +1 glass)
    fun incrementProgress(habit: Habit) = viewModelScope.launch {
        val goal = habit.dailyGoal.toIntOrNull() ?: return@launch
        val current = DayUtils.getTodayProgress(habit.dailyProgress)
        val newValue = (current + 1).coerceAtMost(goal)
        val newProgress = DayUtils.setTodayProgress(habit.dailyProgress, newValue)
        val isNowComplete = newValue >= goal

        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        val newStreak = if (isNowComplete && !habit.isCompletedToday) {
            when (habit.lastCompletedDate) {
                yesterday -> habit.streak + 1
                today     -> habit.streak
                else      -> 1
            }
        } else habit.streak

        val newHistory = if (isNowComplete && !habit.isCompletedToday) {
            if (habit.completionHistory.isEmpty()) today
            else "${habit.completionHistory},$today"
        } else habit.completionHistory

        dao.updateHabit(habit.copy(
            dailyProgress = newProgress,
            isCompletedToday = isNowComplete,
            streak = newStreak,
            lastCompletedDate = if (isNowComplete) today else habit.lastCompletedDate,
            completionHistory = newHistory
        ))
    }

    // ✅ Reset today's progress for a habit
    fun resetTodayProgress(habit: Habit) = viewModelScope.launch {
        val newProgress = DayUtils.setTodayProgress(habit.dailyProgress, 0)
        dao.updateHabit(habit.copy(
            dailyProgress = newProgress,
            isCompletedToday = false
        ))
    }
}