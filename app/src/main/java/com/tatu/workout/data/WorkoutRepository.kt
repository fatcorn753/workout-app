package com.tatu.workout.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.workoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "workout_data")

/** 記録の唯一の置き場。detox-app の DetoxRepository と同じ DataStore + JSON 方式。 */
object WorkoutRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val key = stringPreferencesKey("workout_data_json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var appContext: Context

    private val _data = MutableStateFlow(WorkoutData())
    val data: StateFlow<WorkoutData> = _data.asStateFlow()

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        scope.launch {
            appContext.workoutDataStore.data
                .map { prefs -> prefs[key]?.let { decode(it) } ?: WorkoutData() }
                .collect { _data.value = it }
        }
    }

    private fun decode(raw: String): WorkoutData =
        runCatching { json.decodeFromString(WorkoutData.serializer(), raw) }
            .getOrElse { WorkoutData() }

    /** 読み込み→変換→書き込みを 1 トランザクションで行う共通処理。 */
    private suspend fun update(transform: (WorkoutData) -> WorkoutData) {
        appContext.workoutDataStore.edit { prefs ->
            val existing = prefs[key]?.let { decode(it) } ?: WorkoutData()
            prefs[key] = json.encodeToString(WorkoutData.serializer(), transform(existing))
        }
    }

    /**
     * 呼び出し元(ViewModel の viewModelScope など)がキャンセルされても書き込みが
     * 中断されないよう、自前の [scope] で実行する。画面遷移やプロセスの再構成の
     * タイミングと保存が重なって記録が消える、という事故を避けるための設計。
     */
    fun addSession(reps: Int, durationMillis: Long) {
        val session = PushUpSession(
            id = UUID.randomUUID().toString(),
            atMillis = System.currentTimeMillis(),
            reps = reps,
            durationMillis = durationMillis,
        )
        scope.launch { update { it.copy(sessions = listOf(session) + it.sessions) } }
    }

    fun deleteSession(id: String) {
        scope.launch { update { it.copy(sessions = it.sessions.filterNot { session -> session.id == id }) } }
    }
}
