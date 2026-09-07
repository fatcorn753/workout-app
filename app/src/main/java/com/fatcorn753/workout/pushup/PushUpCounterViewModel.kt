package com.fatcorn753.workout.pushup

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fatcorn753.workout.data.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PushUpCounterViewModel(app: Application) : AndroidViewModel(app) {

    private val repCounter = RepCounter()

    var reps by mutableStateOf(0)
        private set
    var isFaceDetected by mutableStateOf(false)
        private set
    var isRunning by mutableStateOf(false)
        private set
    var elapsedMillis by mutableStateOf(0L)
        private set
    var normalizedPosition by mutableStateOf<Float?>(null)
        private set
    /** 直近保存したセッションの回数。保存完了メッセージの表示に使う。null なら未保存。 */
    var lastSavedReps by mutableStateOf<Int?>(null)
        private set

    private var timerJob: Job? = null
    private var startedAt = 0L

    fun start() {
        repCounter.reset()
        reps = 0
        elapsedMillis = 0
        isFaceDetected = false
        normalizedPosition = null
        lastSavedReps = null
        isRunning = true
        startedAt = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (isActive) {
                elapsedMillis = System.currentTimeMillis() - startedAt
                delay(200)
            }
        }
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        timerJob?.cancel()
        if (reps > 0) {
            WorkoutRepository.addSession(reps, elapsedMillis)
            lastSavedReps = reps
        }
    }

    fun onFaceHeight(heightPx: Float?, timestampMillis: Long) {
        if (!isRunning) return
        isFaceDetected = heightPx != null
        if (heightPx == null) return
        repCounter.onFaceHeight(heightPx, timestampMillis)
        reps = repCounter.repCount
        normalizedPosition = repCounter.normalizedPosition
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
