package com.habittracker.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.habittracker.R
import com.habittracker.data.Habit

class HabitAdapter(
    private val onToggle: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit,
    private val onProgress: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var habits = listOf<Habit>()

    fun submitList(list: List<Habit>) {
        habits = list
        notifyDataSetChanged()
    }

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvHabitName)
        val streak: TextView = view.findViewById(R.id.tvStreak)
        val reminderTime: TextView = view.findViewById(R.id.tvReminderTime)
        val checkBox: CheckBox = view.findViewById(R.id.cbComplete)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnProgress: ImageButton = view.findViewById(R.id.btnProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HabitViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
        )

    override fun getItemCount() = habits.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        // Name + strike-through if completed
        holder.name.text = habit.name
        holder.name.paintFlags = if (habit.isCompletedToday)
            holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // Streak label
        holder.streak.text = when {
            habit.streak >= 30 -> "🏆 ${habit.streak} day streak — legend!"
            habit.streak >= 7  -> "🔥 ${habit.streak} day streak — on fire!"
            habit.streak >= 3  -> "⚡ ${habit.streak} day streak"
            habit.streak == 1  -> "✨ 1 day streak — just started!"
            else               -> "🌱 No streak yet"
        }

        // Reminder time
        if (habit.reminderTime.isNotEmpty()) {
            holder.reminderTime.visibility = View.VISIBLE
            holder.reminderTime.text = "⏰ ${habit.reminderTime}"
        } else {
            holder.reminderTime.visibility = View.GONE
        }

        // Checkbox — safely toggle without double firing
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = habit.isCompletedToday
        holder.checkBox.setOnCheckedChangeListener { _, _ -> onToggle(habit) }
        holder.btnProgress.setOnClickListener {
            onProgress(habit) // already wired — just update MainActivity
        }
        // Buttons
        holder.btnEdit.setOnClickListener { onEdit(habit) }
        holder.btnDelete.setOnClickListener { onDelete(habit) }
        holder.btnProgress.setOnClickListener { onProgress(habit) }
    }
}