package com.habittracker.utils

import java.time.DayOfWeek
import java.time.LocalDate

object DayUtils {

    val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val DAY_FULL   = listOf("Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday", "Sunday")

    // Convert "1,2,3" → setOf(1,2,3)
    fun parseDays(activeDays: String): Set<Int> =
        activeDays.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

    // Convert setOf(1,2,3) → "1,2,3"
    fun encodeDays(days: Set<Int>): String =
        days.sorted().joinToString(",")

    // Today's day number: Mon=1 ... Sun=7
    fun todayDayNumber(): Int =
        LocalDate.now().dayOfWeek.value  // Mon=1, Sun=7

    // Is habit active today?
    fun isActiveToday(activeDays: String): Boolean =
        todayDayNumber() in parseDays(activeDays)

    // Parse daily progress JSON manually (no Gson needed)
    // Format stored: "date1=val1|date2=val2"
    fun parseProgress(raw: String): Map<String, Int> {
        if (raw.isEmpty()) return emptyMap()
        return raw.split("|")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0)
                else null
            }.toMap()
    }

    fun encodeProgress(map: Map<String, Int>): String =
        map.entries.joinToString("|") { "${it.key}=${it.value}" }

    fun getTodayProgress(raw: String): Int {
        val today = LocalDate.now().toString()
        return parseProgress(raw)[today] ?: 0
    }

    fun setTodayProgress(raw: String, value: Int): String {
        val today = LocalDate.now().toString()
        val map = parseProgress(raw).toMutableMap()
        map[today] = value
        return encodeProgress(map)
    }
}