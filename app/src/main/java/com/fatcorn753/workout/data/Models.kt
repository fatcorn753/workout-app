package com.fatcorn753.workout.data

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 1回のトレーニングセッションの記録。 */
@Serializable
data class PushUpSession(
    val id: String,
    val atMillis: Long,
    val reps: Int,
    val durationMillis: Long,
)

fun PushUpSession.localDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

@Serializable
data class WorkoutData(
    val sessions: List<PushUpSession> = emptyList(),
)

fun formatSessionDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
