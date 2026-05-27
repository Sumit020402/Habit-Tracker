package com.habittracker.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.habittracker.R
import com.habittracker.data.Habit
import com.habittracker.utils.DayUtils

class HabitAdapter(
    private val onToggle: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit,
    private val onProgress: (Habit) -> Unit,
    private val onIncrement: (Habit) -> Unit,
    private val onReset: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var habits = listOf<Habit>()

    fun submitList(list: List<Habit>) {
        habits = list
        notifyDataSetChanged()
    }

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView           = view.findViewById(R.id.tvHabitName)
        val streak: TextView         = view.findViewById(R.id.tvStreak)
        val reminderTime: TextView   = view.findViewById(R.id.tvReminderTime)
        val checkBox: CheckBox       = view.findViewById(R.id.cbComplete)
        val btnDelete: ImageButton   = view.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton     = view.findViewById(R.id.btnEdit)
        val btnProgress: ImageButton = view.findViewById(R.id.btnProgress)

        // Day chips
        val dayChips: List<TextView> = listOf(
            view.findViewById(R.id.tvDay1),
            view.findViewById(R.id.tvDay2),
            view.findViewById(R.id.tvDay3),
            view.findViewById(R.id.tvDay4),
            view.findViewById(R.id.tvDay5),
            view.findViewById(R.id.tvDay6),
            view.findViewById(R.id.tvDay7)
        )

        // Goal views
        val goalRow: LinearLayout       = view.findViewById(R.id.goalRow)
        val goalProgressBar: ProgressBar = view.findViewById(R.id.goalProgressBar)
        val tvGoalProgress: TextView    = view.findViewById(R.id.tvGoalProgress)
        val btnIncrement: Button        = view.findViewById(R.id.btnIncrement)
        val btnReset: Button            = view.findViewById(R.id.btnReset)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HabitViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
        )

    override fun getItemCount() = habits.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        val activeDays = DayUtils.parseDays(habit.activeDays)
        val todayNum = DayUtils.todayDayNumber()
        val isActiveToday = todayNum in activeDays

        // ── Name ──────────────────────────────────────────────────
        holder.name.text = habit.name
        holder.name.paintFlags = if (habit.isCompletedToday)
            holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // ── Streak ────────────────────────────────────────────────
        holder.streak.text = when {
            habit.streak >= 30 -> "🏆 ${habit.streak} day streak"
            habit.streak >= 7  -> "🔥 ${habit.streak} day streak"
            habit.streak >= 3  -> "⚡ ${habit.streak} day streak"
            habit.streak == 1  -> "✨ 1 day streak"
            else               -> "🌱 No streak yet"
        }

        // ── Reminder ──────────────────────────────────────────────
        if (habit.reminderTime.isNotEmpty()) {
            holder.reminderTime.visibility = View.VISIBLE
            holder.reminderTime.text = "⏰ ${habit.reminderTime}"
        } else {
            holder.reminderTime.visibility = View.GONE
        }

        // ── Day Chips ─────────────────────────────────────────────
        val labels = DayUtils.DAY_LABELS
        holder.dayChips.forEachIndexed { index, chip ->
            val dayNum = index + 1 // Mon=1 ... Sun=7
            val isActive = dayNum in activeDays
            val isToday  = dayNum == todayNum

            chip.text = labels[index]

            chip.background = chip.context.getDrawable(
                when {
                    isActive && isToday  -> R.drawable.bg_day_chip_active      // filled purple
                    isActive             -> R.drawable.bg_day_chip_active      // filled purple
                    isToday              -> R.drawable.bg_day_chip_inactive_today // outlined
                    else                 -> R.drawable.bg_day_chip_inactive    // grey
                }
            )

            chip.setTextColor(
                chip.context.getColor(
                    if (isActive) android.R.color.white
                    else android.R.color.darker_gray
                )
            )

            // Tap a chip to toggle that day active/inactive
            chip.setOnClickListener {
                val newDays = activeDays.toMutableSet()
                if (dayNum in newDays) newDays.remove(dayNum)
                else newDays.add(dayNum)
                // Don't allow zero days selected
                if (newDays.isNotEmpty()) {
                    onEdit(habit.copy(activeDays = DayUtils.encodeDays(newDays)))
                }
            }
        }

        // ── Checkbox ──────────────────────────────────────────────
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = habit.isCompletedToday
        holder.checkBox.isEnabled = isActiveToday
        holder.checkBox.alpha = if (isActiveToday) 1f else 0.4f
        holder.checkBox.setOnCheckedChangeListener { _, _ ->
            if (isActiveToday) onToggle(habit)
        }

        // ── Goal Progress ─────────────────────────────────────────
        val goal = habit.dailyGoal.toIntOrNull()
        if (goal != null && goal > 0) {
            holder.goalRow.visibility = View.VISIBLE
            val current = DayUtils.getTodayProgress(habit.dailyProgress)
            val percent = ((current.toFloat() / goal) * 100).toInt()

            holder.goalProgressBar.progress = percent
            holder.tvGoalProgress.text =
                "$current / $goal ${habit.goalUnit}  ($percent%)"

            holder.btnIncrement.isEnabled = current < goal && isActiveToday
            holder.btnIncrement.alpha = if (current < goal && isActiveToday) 1f else 0.4f

            holder.btnIncrement.setOnClickListener { onIncrement(habit) }
            holder.btnReset.setOnClickListener { onReset(habit) }
        } else {
            holder.goalRow.visibility = View.GONE
        }

        // ── Buttons ───────────────────────────────────────────────
        holder.btnEdit.setOnClickListener { onEdit(habit) }
        holder.btnDelete.setOnClickListener { onDelete(habit) }
        holder.btnProgress.setOnClickListener { onProgress(habit) }
    }
}