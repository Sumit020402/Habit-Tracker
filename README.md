<div align="center">

# 🏆 HabitTracker

### *Build better habits. One day at a time.*

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/Min%20SDK-API%2026-orange?style=for-the-badge)
![Room](https://img.shields.io/badge/Database-Room-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

<br/>

<img src="https://img.shields.io/badge/Status-Active%20Development-brightgreen?style=flat-square"/>
<img src="https://img.shields.io/badge/Version-1.0.0-purple?style=flat-square"/>
<img src="https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square"/>

</div>

---

## 📖 Overview

**HabitTracker** is a beautifully designed Android application that helps you build and maintain positive habits in your daily life. With an intuitive interface, smart streak tracking, daywise scheduling, and goal-based progress — it's your personal accountability partner, right in your pocket.

> *"We are what we repeatedly do. Excellence, then, is not an act, but a habit."*
> — Aristotle

---

## ✨ Features

### 📋 Daily Habit Checklist
- Add unlimited habits with names and descriptions
- Check off habits as you complete them each day
- Strike-through animation on completion
- Empty state with friendly prompt when no habits exist

### 🔥 Streak Counter
- Automatic streak calculation based on daily completions
- Smart streak recovery — uncheck today to revert streak
- Visual streak badges: 🌱 Starting → ⚡ Building → 🔥 On fire → 🏆 Legend
- Streak resets intelligently when days are missed

### 📅 Daywise Habit Scheduler
- Set habits to repeat on specific days of the week
- Beautiful **Mon–Sun day chips** displayed on each habit card
- Tap any chip directly on the card to toggle that day
- Habits are automatically disabled on non-active days
- Default Mon–Fri scheduling for work habits

### 🎯 Daily Goal Tracking
- Set measurable daily targets (e.g. 8 glasses, 30 minutes, 10 pages)
- Visual progress bar on each habit card
- **+1 increment button** to log progress step by step
- Auto-completes habit when goal is reached
- Reset button to restart the day's progress

### ⏰ Smart Notifications
- Set a daily reminder time for each habit
- Reliable delivery using `setExactAndAllowWhileIdle`
- Notification permission handled gracefully on Android 13+
- Custom notification icon with habit name in the alert

### 📊 Progress Charts
- Bar chart showing last 7 days of completion history
- Stats overview: current streak, total completions, this month's count
- Powered by **MPAndroidChart**

### 📆 Calendar History View
- Full scrollable calendar showing 12 months of history
- 🟣 Purple filled circle = completed day
- ⭕ Outlined circle = today (not yet completed)
- 🔴 Red dot = missed day
- Color legend at the bottom for quick reference

### ✏️ Edit & Delete
- Edit any habit's name, description, days, goal, or reminder
- Pre-filled form when editing for seamless experience
- Confirmation dialog before deletion to prevent accidents

### 🎨 Beautiful Theming
- Material Design 3 components throughout
- Full **dark mode** support (auto follows system)
- Poppins font for clean modern typography
- Consistent purple + teal color palette
- Rounded card corners and smooth elevation shadows

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room (SQLite) |
| **Async** | Kotlin Coroutines + ViewModelScope |
| **UI** | Material Design 3, RecyclerView |
| **Charts** | MPAndroidChart |
| **Calendar** | Kizitonwose Calendar View |
| **DI Pattern** | Manual (no Hilt) |
| **Navigation** | Intent-based Activity navigation |
| **Notifications** | AlarmManager + BroadcastReceiver |

---

## 📁 Project Structure

```
HabitTracker/
├── app/src/main/
│   ├── java/com/habittracker/
│   │   ├── data/
│   │   │   ├── Habit.kt                 # Room Entity (Parcelable)
│   │   │   ├── HabitDao.kt              # Database queries
│   │   │   └── HabitDatabase.kt         # Room database singleton
│   │   │
│   │   ├── ui/
│   │   │   ├── MainActivity.kt          # Habit list + FAB
│   │   │   ├── AddHabitActivity.kt      # Add / Edit habit form
│   │   │   ├── ProgressActivity.kt      # Bar chart progress screen
│   │   │   ├── CalendarActivity.kt      # Calendar history view
│   │   │   └── HabitAdapter.kt          # RecyclerView adapter
│   │   │
│   │   ├── viewmodel/
│   │   │   └── HabitViewModel.kt        # Business logic + DB ops
│   │   │
│   │   ├── notifications/
│   │   │   └── ReminderReceiver.kt      # BroadcastReceiver for alarms
│   │   │
│   │   └── utils/
│   │       └── DayUtils.kt              # Day encoding/decoding helpers
│   │
│   └── res/
│       ├── layout/                      # XML layouts
│       ├── drawable/                    # Shapes, backgrounds, icons
│       ├── values/                      # Colors, themes, dimensions
│       ├── values-night/                # Dark mode overrides
│       └── font/                        # Poppins font family
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio **Hedgehog** or later
- JDK 11+
- Android device or emulator running **API 26+**

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/HabitTracker.git
cd HabitTracker
```

**2. Open in Android Studio**
```
File → Open → Select the HabitTracker folder
```

**3. Add JitPack to `settings.gradle.kts`**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**4. Sync Gradle**
```
File → Sync Project with Gradle Files
```

**5. Run the app**
```
Click ▶️ Run or press Shift + F10
```

---

## 📦 Dependencies

```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.activity:activity-ktx:1.8.2")
implementation("androidx.fragment:fragment-ktx:1.6.2")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// ViewModel + LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Charts
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// Calendar
implementation("com.kizitonwose.calendar:view:2.5.0")
```

---

## 🔐 Permissions

```xml
<!-- Daily reminders -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

---

## 🗺️ Roadmap

- [x] Daily habit checklist
- [x] Streak counter with smart logic
- [x] Push notifications / reminders
- [x] Progress bar charts
- [x] Calendar history view
- [x] Edit & delete habits
- [x] Daywise scheduling (Mon–Sun chips)
- [x] Goal-based tracking (+1 increment)
- [x] Dark mode support
- [x] Material You theming
- [ ] Widget for home screen
- [ ] Habit categories / tags
- [ ] Weekly & monthly report
- [ ] Cloud backup (Firebase)
- [ ] Google Play release
- [ ] Multi-language support

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

```bash
# Fork the repo, then:
git checkout -b feature/your-feature-name
git commit -m "Add: your feature description"
git push origin feature/your-feature-name
# Open a Pull Request
```

Please follow the existing code style and include comments for any new logic.

---

## 🐛 Known Issues

| Issue | Status | Fix |
|-------|--------|-----|
| `SCHEDULE_EXACT_ALARM` crash on Android 12+ | ✅ Fixed | Use `setExactAndAllowWhileIdle` |
| CalendarView missing `cv_dayViewResource` | ✅ Fixed | Added attribute in XML |
| `by viewModels()` unresolved | ✅ Fixed | Added `activity-ktx` dependency |

---

## 📄 License

```
MIT License

Copyright (c) 2026 HabitTracker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 👨‍💻 Author

<div align="center">

Built with ❤️ using **Kotlin** + **Android Studio**

*If this project helped you, please give it a ⭐ on GitHub!*

</div>
