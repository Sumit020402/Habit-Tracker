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
    val completionHistory: String = ""
) : Parcelable