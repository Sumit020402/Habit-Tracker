package com.habittracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.habittracker.R
import com.habittracker.data.Habit
import com.habittracker.viewmodel.HabitViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: HabitViewModel by viewModels()
    private lateinit var adapter: HabitAdapter

    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeHabits()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.toolbar
        )
        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_progress) {
                startActivity(Intent(this, ProgressActivity::class.java))
                true
            } else false
        }
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            onToggle   = { habit -> viewModel.toggleComplete(habit) },
            onDelete   = { habit -> confirmDelete(habit) },
            onEdit     = { habit -> openEditActivity(habit) },
            onProgress = { habit ->
                val intent = Intent(this, CalendarActivity::class.java).apply {
                    putExtra("habit_name", habit.name)
                    putExtra("streak", habit.streak)
                    putExtra("history", habit.completionHistory)
                }
                startActivity(intent)
            },
            onIncrement = { habit -> viewModel.incrementProgress(habit) }, // ✅ NEW
            onReset     = { habit -> viewModel.resetTodayProgress(habit) } // ✅ NEW
        )

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }
    }

    private fun observeHabits() {
        viewModel.allHabits.observe(this) { habits ->
            adapter.submitList(habits)
            val emptyState = findViewById<View>(R.id.emptyState)
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            if (habits.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun openEditActivity(habit: Habit) {
        val intent = Intent(this, AddHabitActivity::class.java).apply {
            putExtra("habit", habit)
        }
        startActivity(intent)
    }

    private fun confirmDelete(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteHabit(habit) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}