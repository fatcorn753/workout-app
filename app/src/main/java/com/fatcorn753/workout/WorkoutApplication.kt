package com.fatcorn753.workout

import android.app.Application
import com.fatcorn753.workout.data.WorkoutRepository

class WorkoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkoutRepository.init(this)
    }
}
