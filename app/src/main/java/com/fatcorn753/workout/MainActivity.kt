package com.fatcorn753.workout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fatcorn753.workout.pushup.PushUpCounterScreen
import com.fatcorn753.workout.ui.HomeScreen
import com.fatcorn753.workout.ui.RecordsScreen
import com.fatcorn753.workout.ui.theme.WorkoutTheme

private enum class Screen { HOME, COUNTER, RECORDS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutTheme {
                WorkoutApp()
            }
        }
    }
}

@Composable
private fun WorkoutApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }

    // ホーム以外の画面ではシステムの戻る操作をアプリ内遷移として扱う。
    // 無効化しない限り、素の戻るキーはこの Activity ごと閉じてしまう。
    BackHandler(enabled = screen != Screen.HOME) {
        screen = Screen.HOME
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            onStartPushUps = { screen = Screen.COUNTER },
            onShowRecords = { screen = Screen.RECORDS },
        )
        Screen.COUNTER -> PushUpCounterScreen(onBack = { screen = Screen.HOME })
        Screen.RECORDS -> RecordsScreen(onBack = { screen = Screen.HOME })
    }
}
