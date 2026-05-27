package com.habittracker.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val streak: Int = 0,
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String = "",
    val reminderTime: String = "",
    val completionHistory: String = "",

    // ✅ NEW — Daywise fields
    val activeDays: String = "1,2,3,4,5,6,7",   // 1=Mon...7=Sun, all days default
    val dailyGoal: String = "",                   // e.g. "8" glasses, "30" mins
    val goalUnit: String = "",                    // e.g. "glasses", "minutes", "times"
    val dailyProgress: String = ""                // JSON: {"2026-05-27":"5"}
) : Parcelable