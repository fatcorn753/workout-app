package com.tatu.workout

import android.app.Application
import com.tatu.workout.data.WorkoutRepository

class WorkoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkoutRepository.init(this)
    }
}
