package com.example.firstapp

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firstapp.ui.theme.AppAccentColor
import com.example.firstapp.ui.theme.BlueAccent
import com.example.firstapp.ui.theme.AppThemeMode
import com.example.firstapp.ui.theme.FirstAppTheme
import com.example.firstapp.ui.theme.GreenAccent
import com.example.firstapp.ui.theme.PurpleAccent
import com.example.firstapp.ui.theme.RedAccent
import kotlinx.coroutines.delay

private const val TimersPerPage = 4

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimerApp()
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
    val context = LocalContext.current
    val timers = remember { mutableStateListOf<TimerItem>() }
    val finishedTimerIds = remember { mutableStateListOf<Int>() }
    var nextTimerId by rememberSaveable { mutableIntStateOf(1) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var isBuilderOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var themeModeName by rememberSaveable { mutableStateOf(AppThemeMode.DARK.name) }
    var accentColorName by rememberSaveable { mutableStateOf(AppAccentColor.RED.name) }
    var activeAlertRingtone by remember { mutableStateOf<Ringtone?>(null) }
    val themeMode = AppThemeMode.valueOf(themeModeName)
    val accentColor = AppAccentColor.valueOf(accentColorName)

    FirstAppTheme(themeMode = themeMode, accentColor = accentColor) {
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
                        if (timer.remainingSeconds > 0 && updatedRemaining == 0 && !finishedTimerIds.contains(timer.id)) {
                            finishedTimerIds.add(timer.id)
                        }
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
        val activeFinishedTimerId = finishedTimerIds.firstOrNull()

        LaunchedEffect(timers.size, pageCount) {
            currentPage = currentPage.coerceAtMost(pageCount - 1)
        }

        LaunchedEffect(activeFinishedTimerId) {
            activeAlertRingtone?.stop()
            activeAlertRingtone = null

            if (activeFinishedTimerId != null) {
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                activeAlertRingtone = ringtoneUri
                    ?.let { RingtoneManager.getRingtone(context, it) }
                    ?.also { it.play() }
            }
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
            SettingsDialog(
                selectedThemeMode = themeMode,
                selectedAccentColor = accentColor,
                onThemeSelected = { selectedMode ->
                    themeModeName = selectedMode.name
                },
                onAccentSelected = { selectedAccent ->
                    accentColorName = selectedAccent.name
                },
                onDismiss = { isSettingsOpen = false }
            )
        }

        if (activeFinishedTimerId != null) {
            FinishedTimerDialog(
                onStopAlert = {
                    activeAlertRingtone?.stop()
                    activeAlertRingtone = null
                    finishedTimerIds.remove(activeFinishedTimerId)
                }
            )
        }
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
        AnimatedOutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .width(50.dp)
                .height(46.dp),
            shape = RoundedCornerShape(16.dp),
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
        AnimatedButton(
            onClick = onOpenBuilder,
            modifier = Modifier
                .weight(1.3f)
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(text = "Custom", style = MaterialTheme.typography.titleMedium)
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
    AnimatedOutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
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
    val isEmptySlot = timer == null
    val slotColor = if (isEmptySlot) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val slotBorder = if (isEmptySlot) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.9f))
    } else {
        null
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = slotColor),
        border = slotBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isEmptySlot) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Empty slot",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ready for timer",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                var showTimerContent by remember(timer.id) { mutableStateOf(false) }

                LaunchedEffect(timer.id) {
                    showTimerContent = true
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showTimerContent,
                    enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                        scaleIn(
                            initialScale = 0.84f,
                            animationSpec = tween(durationMillis = 220)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedButton(
                            onClick = { onRemove(timer.id) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .width(38.dp)
                                .height(38.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("X", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(end = 50.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = formatClock(timer.remainingSeconds),
                                    style = TextStyle(
                                        fontSize = 64.sp,
                                        lineHeight = 64.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Start,
                                    maxLines = 1
                                )
                            }

                            if (timer.isRunning) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnimatedButton(
                                        onClick = { onStartPause(timer.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 46.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Text("Pause", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    }
                                    AnimatedOutlinedButton(
                                        onClick = { onReset(timer.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 46.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Text("Reset", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    }
                                }
                            } else {
                                AnimatedButton(
                                    onClick = { onStartPause(timer.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 46.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (timer.remainingSeconds == 0) "Restart" else "Start",
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                }
                            }
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
        AnimatedOutlinedButton(
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

        AnimatedOutlinedButton(
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
                AnimatedTextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Cancel")
                }

                AnimatedButton(
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
private fun SettingsDialog(
    selectedThemeMode: AppThemeMode,
    selectedAccentColor: AppAccentColor,
    onThemeSelected: (AppThemeMode) -> Unit,
    onAccentSelected: (AppAccentColor) -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(onDismiss = onDismiss) {
        DialogCard(title = "Settings", subtitle = "") {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ThemeModeToggle(
                selectedThemeMode = selectedThemeMode,
                onThemeSelected = onThemeSelected
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Accent",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            AccentColorToggle(
                selectedAccentColor = selectedAccentColor,
                onAccentSelected = onAccentSelected
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun FinishedTimerDialog(
    onStopAlert: () -> Unit
) {
    AppDialog(onDismiss = onStopAlert) {
        DialogCard(
            title = "Timer Finished",
            subtitle = "A timer has completed. Stop the alert when you are ready."
        ) {
            AnimatedButton(
                onClick = onStopAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Stop Alert")
            }
        }
    }
}

@Composable
private fun AccentColorToggle(
    selectedAccentColor: AppAccentColor,
    onAccentSelected: (AppAccentColor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AccentColorButton(
            accentColor = AppAccentColor.RED,
            isSelected = selectedAccentColor == AppAccentColor.RED,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.RED) }
        )
        AccentColorButton(
            accentColor = AppAccentColor.BLUE,
            isSelected = selectedAccentColor == AppAccentColor.BLUE,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.BLUE) }
        )
        AccentColorButton(
            accentColor = AppAccentColor.GREEN,
            isSelected = selectedAccentColor == AppAccentColor.GREEN,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.GREEN) }
        )
        AccentColorButton(
            accentColor = AppAccentColor.PURPLE,
            isSelected = selectedAccentColor == AppAccentColor.PURPLE,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.PURPLE) }
        )
    }
}

@Composable
private fun AccentColorButton(
    accentColor: AppAccentColor,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .heightIn(min = 38.dp)
            .pressScale(interactionSource, pressedScale = 0.94f)
            .clip(shape)
            .background(accentSwatchColor(accentColor), shape)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
                },
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}

private fun accentSwatchColor(accentColor: AppAccentColor): Color = when (accentColor) {
    AppAccentColor.RED -> RedAccent
    AppAccentColor.BLUE -> BlueAccent
    AppAccentColor.GREEN -> GreenAccent
    AppAccentColor.PURPLE -> PurpleAccent
}

@Composable
private fun ThemeModeToggle(
    selectedThemeMode: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeModeButton(
            label = "Dark",
            isSelected = selectedThemeMode == AppThemeMode.DARK,
            modifier = Modifier.weight(1f),
            onClick = { onThemeSelected(AppThemeMode.DARK) }
        )
        ThemeModeButton(
            label = "Light",
            isSelected = selectedThemeMode == AppThemeMode.LIGHT,
            modifier = Modifier.weight(1f),
            onClick = { onThemeSelected(AppThemeMode.LIGHT) }
        )
        ThemeModeButton(
            label = "OLED",
            isSelected = selectedThemeMode == AppThemeMode.OLED,
            modifier = Modifier.weight(1f),
            onClick = { onThemeSelected(AppThemeMode.OLED) }
        )
    }
}

@Composable
private fun ThemeModeButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (isSelected) {
        AnimatedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 40.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    } else {
        AnimatedOutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 40.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
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
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
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
    AnimatedOutlinedButton(
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
    AnimatedOutlinedButton(
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

@Composable
private fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun AnimatedOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun AnimatedTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    TextButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "button_press_scale"
    )

    return this.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
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

@Preview(showBackground = true, widthDp = 360, heightDp = 412)
@Composable
private fun TimerAppPreview() {
    FirstAppTheme {
        TimerApp()
    }
}
