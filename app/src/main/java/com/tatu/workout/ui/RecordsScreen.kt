package com.tatu.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tatu.workout.data.PushUpSession
import com.tatu.workout.data.WorkoutRepository
import com.tatu.workout.data.formatSessionDuration
import com.tatu.workout.data.localDate
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.Locale

private val WeekdayLabels = listOf("日", "月", "火", "水", "木", "金", "土")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(onBack: () -> Unit) {
    val data by WorkoutRepository.data.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val sessionsByDate = remember(data.sessions) {
        data.sessions.groupBy { it.localDate() }
    }
    val totalRepsByDate = remember(sessionsByDate) {
        sessionsByDate.mapValues { (_, sessions) -> sessions.sumOf { it.reps } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("記録") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MonthHeader(
                yearMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            )
            CalendarGrid(
                yearMonth = currentMonth,
                totalRepsByDate = totalRepsByDate,
                onDayClick = { date -> if (sessionsByDate.containsKey(date)) selectedDate = date },
            )
        }
    }

    val date = selectedDate
    if (date != null) {
        DayDetailDialog(
            date = date,
            sessions = sessionsByDate[date].orEmpty(),
            onDismiss = { selectedDate = null },
            onDeleteEmpty = { selectedDate = null },
        )
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前の月")
        }
        Text(
            "${yearMonth.year}年${yearMonth.monthValue}月",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "次の月")
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    totalRepsByDate: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val leadingBlanks = firstDayOfMonth.dayOfWeek.value % 7 // 0=日 .. 6=土
    val daysInMonth = yearMonth.lengthOfMonth()
    val trailingBlanks = (7 - (leadingBlanks + daysInMonth) % 7) % 7
    val cells: List<LocalDate?> = buildList {
        repeat(leadingBlanks) { add(null) }
        for (day in 1..daysInMonth) add(yearMonth.atDay(day))
        repeat(trailingBlanks) { add(null) }
    }
    val today = LocalDate.now()

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WeekdayLabels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
        ) {
            gridItems(cells) { date ->
                DayCell(
                    date = date,
                    totalReps = date?.let { totalRepsByDate[it] },
                    isToday = date == today,
                    onClick = { date?.let(onDayClick) },
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    totalReps: Int?,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(
                if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .clickable(enabled = date != null && totalReps != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${date.dayOfMonth}",
                    fontSize = 14.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
                if (totalReps != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(22.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$totalReps",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    sessions: List<PushUpSession>,
    onDismiss: () -> Unit,
    onDeleteEmpty: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "${date.monthValue}月${date.dayOfMonth}日の記録",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "合計 ${sessions.sumOf { it.reps }}回",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )

                if (sessions.isEmpty()) {
                    Text(
                        "この日の記録はありません",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    val sortedSessions = remember(sessions) { sessions.sortedByDescending { it.atMillis } }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(sortedSessions, key = { it.id }) { session ->
                            SessionRow(
                                session = session,
                                onDelete = {
                                    WorkoutRepository.deleteSession(session.id)
                                    // 表示中の最後の 1 件を消したら、空になるこのダイアログを閉じる
                                    if (sessions.size <= 1) onDeleteEmpty()
                                },
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                ) {
                    Text("閉じる")
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: PushUpSession, onDelete: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.JAPAN) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${session.reps}回",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${timeFormat.format(Date(session.atMillis))} · ${formatSessionDuration(session.durationMillis)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除")
            }
        }
    }
}
