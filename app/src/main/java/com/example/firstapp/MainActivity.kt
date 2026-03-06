package com.example.firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firstapp.ui.theme.FirstAppTheme
import kotlinx.coroutines.delay

private const val TimersPerPage = 4

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                TimerApp()
            }
        }
    }
}

private data class TimerItem(
    val id: Int,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = false,
    val endTimeMillis: Long? = null
)

@Composable
fun TimerApp() {
    val timers = remember { mutableStateListOf<TimerItem>() }
    var nextTimerId by rememberSaveable { mutableIntStateOf(1) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var isBuilderOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()

            for (index in timers.indices) {
                val timer = timers[index]
                if (!timer.isRunning || timer.endTimeMillis == null) continue

                val updatedRemaining = ((timer.endTimeMillis - now + 999L) / 1000L)
                    .toInt()
                    .coerceAtLeast(0)

                if (updatedRemaining != timer.remainingSeconds || updatedRemaining == 0) {
                    timers[index] = timer.copy(
                        remainingSeconds = updatedRemaining,
                        isRunning = updatedRemaining > 0,
                        endTimeMillis = if (updatedRemaining > 0) timer.endTimeMillis else null
                    )
                }
            }

            delay(if (timers.any { it.isRunning }) 250 else 600)
        }
    }

    fun addTimer(totalSeconds: Int, startImmediately: Boolean = false) {
        if (totalSeconds <= 0) return

        val endTimeMillis = if (startImmediately) {
            System.currentTimeMillis() + totalSeconds * 1000L
        } else {
            null
        }

        timers.add(
            0,
            TimerItem(
                id = nextTimerId,
                totalSeconds = totalSeconds,
                remainingSeconds = totalSeconds,
                isRunning = startImmediately,
                endTimeMillis = endTimeMillis
            )
        )
        nextTimerId += 1
        currentPage = 0
    }

    fun updateTimer(timerId: Int, transform: (TimerItem) -> TimerItem) {
        val index = timers.indexOfFirst { it.id == timerId }
        if (index >= 0) {
            timers[index] = transform(timers[index])
        }
    }

    val pageCount = ((timers.size + TimersPerPage - 1) / TimersPerPage).coerceAtLeast(1)
    val visibleTimers = timers.drop(currentPage * TimersPerPage).take(TimersPerPage)

    LaunchedEffect(timers.size, pageCount) {
        currentPage = currentPage.coerceAtMost(pageCount - 1)
    }

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DashboardScreen(
                currentPage = currentPage,
                pageCount = pageCount,
                visibleTimers = visibleTimers,
                onOpenSettings = { isSettingsOpen = true },
                onOpenBuilder = { isBuilderOpen = true },
                onQuickAdd = { seconds -> addTimer(seconds, startImmediately = true) },
                onStartPause = { timerId ->
                    updateTimer(timerId) { current ->
                        if (current.isRunning) {
                            val remaining = current.endTimeMillis
                                ?.let { endTime ->
                                    ((endTime - System.currentTimeMillis() + 999L) / 1000L)
                                        .toInt()
                                        .coerceAtLeast(0)
                                }
                                ?: current.remainingSeconds

                            current.copy(
                                remainingSeconds = remaining,
                                isRunning = false,
                                endTimeMillis = null
                            )
                        } else {
                            val secondsToRun = if (current.remainingSeconds == 0) {
                                current.totalSeconds
                            } else {
                                current.remainingSeconds
                            }

                            current.copy(
                                remainingSeconds = secondsToRun,
                                isRunning = true,
                                endTimeMillis = System.currentTimeMillis() + secondsToRun * 1000L
                            )
                        }
                    }
                },
                onReset = { timerId ->
                    updateTimer(timerId) { current ->
                        current.copy(
                            remainingSeconds = current.totalSeconds,
                            isRunning = false,
                            endTimeMillis = null
                        )
                    }
                },
                onRemove = { timerId ->
                    timers.removeAll { it.id == timerId }
                },
                onPreviousPage = { currentPage = (currentPage - 1).coerceAtLeast(0) },
                onNextPage = { currentPage = (currentPage + 1).coerceAtMost(pageCount - 1) }
            )
        }
    }

    if (isBuilderOpen) {
        TimerBuilderDialog(
            onDismiss = { isBuilderOpen = false },
            onAddTimer = { totalSeconds ->
                addTimer(totalSeconds)
                isBuilderOpen = false
            }
        )
    }

    if (isSettingsOpen) {
        SettingsDialog(onDismiss = { isSettingsOpen = false })
    }
}

@Composable
private fun DashboardScreen(
    currentPage: Int,
    pageCount: Int,
    visibleTimers: List<TimerItem>,
    onOpenSettings: () -> Unit,
    onOpenBuilder: () -> Unit,
    onQuickAdd: (Int) -> Unit,
    onStartPause: (Int) -> Unit,
    onReset: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HeaderSection(onOpenSettings = onOpenSettings)
        ActionSection(
            onOpenBuilder = onOpenBuilder,
            onQuickAdd = onQuickAdd
        )
        TimerGrid(
            timers = visibleTimers,
            onStartPause = onStartPause,
            onReset = onReset,
            onRemove = onRemove,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        if (pageCount > 1) {
            PageControls(
                currentPage = currentPage,
                pageCount = pageCount,
                canGoBack = currentPage > 0,
                canGoForward = currentPage < pageCount - 1,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage
            )
        }
    }
}

@Composable
private fun HeaderSection(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Multi Timer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .width(42.dp)
                .height(38.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActionSection(
    onOpenBuilder: () -> Unit,
    onQuickAdd: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onOpenBuilder,
            modifier = Modifier
                .weight(1.3f)
                .heightIn(min = 46.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(text = "Custom", style = MaterialTheme.typography.titleSmall)
        }

        QuickAddButton(
            label = "5m",
            modifier = Modifier.weight(1f),
            onClick = { onQuickAdd(300) }
        )
        QuickAddButton(
            label = "10m",
            modifier = Modifier.weight(1f),
            onClick = { onQuickAdd(600) }
        )
        QuickAddButton(
            label = "15m",
            modifier = Modifier.weight(1f),
            onClick = { onQuickAdd(900) }
        )
    }
}

@Composable
private fun QuickAddButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TimerGrid(
    timers: List<TimerItem>,
    onStartPause: (Int) -> Unit,
    onReset: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val slots = buildList<TimerItem?> {
        addAll(timers)
        repeat((TimersPerPage - timers.size).coerceAtLeast(0)) {
            add(null)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slots.chunked(2).forEach { rowTimers ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTimers.forEach { timer ->
                    TimerSlot(
                        timer = timer,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onStartPause = onStartPause,
                        onReset = onReset,
                        onRemove = onRemove
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerSlot(
    timer: TimerItem?,
    modifier: Modifier = Modifier,
    onStartPause: (Int) -> Unit,
    onReset: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    val slotColor = if (timer == null) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = slotColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (timer == null) {
                Text(
                    text = "Empty slot",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(26.dp))
                        Text(
                            text = formatClock(timer.remainingSeconds),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Button(
                            onClick = { onRemove(timer.id) },
                            modifier = Modifier
                                .width(26.dp)
                                .height(26.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("X", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (timer.isRunning) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { onStartPause(timer.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 30.dp),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Pause", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onReset(timer.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 30.dp),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Reset", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onStartPause(timer.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 30.dp),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (timer.remainingSeconds == 0) "Restart" else "Start",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageControls(
    currentPage: Int,
    pageCount: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPreviousPage,
            enabled = canGoBack,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Previous")
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Page ${currentPage + 1} / $pageCount",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        OutlinedButton(
            onClick = onNextPage,
            enabled = canGoForward,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun TimerBuilderDialog(
    onDismiss: () -> Unit,
    onAddTimer: (Int) -> Unit
) {
    var minutes by rememberSaveable { mutableIntStateOf(0) }
    var seconds by rememberSaveable { mutableIntStateOf(30) }

    AppDialog(onDismiss = onDismiss) {
        DialogCard(title = "Create Timer", subtitle = "Set a timer without leaving the main screen.") {
            CompactStepperRow(
                label = "Min",
                value = minutes,
                onDecrease = { minutes = (minutes - 1).coerceAtLeast(0) },
                onIncrease = { minutes = (minutes + 1).coerceAtMost(99) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactStepperRow(
                label = "Sec",
                value = seconds,
                onDecrease = { seconds = (seconds - 15).coerceAtLeast(0) },
                onIncrease = { seconds = (seconds + 15).coerceAtMost(45) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onAddTimer(minutes * 60 + seconds) },
                    enabled = minutes > 0 || seconds > 0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Add Timer")
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    AppDialog(onDismiss = onDismiss) {
        DialogCard(title = "Settings", subtitle = "") {
            Spacer(modifier = Modifier.height(120.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun AppDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun DialogCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(text = "-", onClick = onDecrease)

        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$label: ${value.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        StepperButton(text = "+", onClick = onIncrease)
    }
}

@Composable
private fun CompactStepperRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactStepperButton(text = "-", onClick = onDecrease)
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$label: ${value.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        CompactStepperButton(text = "+", onClick = onIncrease)
    }
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(64.dp)
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun CompactStepperButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(56.dp)
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return when {
        minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

private fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 360)
@Composable
private fun TimerAppPreview() {
    FirstAppTheme {
        TimerApp()
    }
}
