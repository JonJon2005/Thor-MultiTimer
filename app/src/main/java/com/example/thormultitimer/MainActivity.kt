package com.example.thormultitimer

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
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
import com.example.thormultitimer.ui.theme.AppAccentColor
import com.example.thormultitimer.ui.theme.BlueAccent
import com.example.thormultitimer.ui.theme.AppThemeMode
import com.example.thormultitimer.ui.theme.ControllerHighlightColor
import com.example.thormultitimer.ui.theme.GreenAccent
import com.example.thormultitimer.ui.theme.PurpleAccent
import com.example.thormultitimer.ui.theme.RedAccent
import com.example.thormultitimer.ui.theme.ThorMultiTimerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TimersPerPage = 4
private val SettingsColorButtonMinHeight = 38.dp

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

private enum class ControllerInput {
    LEFT,
    RIGHT,
    UP,
    DOWN,
    PRIMARY,
    BACK,
    SECONDARY,
    TERTIARY
}

private enum class DashboardFocusTarget {
    SETTINGS,
    CUSTOM,
    PRESET_FIVE,
    PRESET_TEN,
    PRESET_FIFTEEN,
    TIMER_0,
    TIMER_1,
    TIMER_2,
    TIMER_3,
    PREVIOUS_PAGE,
    NEXT_PAGE
}

private enum class BuilderDialogFocusTarget {
    DIGIT_1,
    DIGIT_2,
    DIGIT_3,
    DIGIT_4,
    DIGIT_5,
    DIGIT_6,
    DIGIT_7,
    DIGIT_8,
    DIGIT_9,
    CLEAR,
    DIGIT_0,
    DELETE,
    CANCEL,
    ADD_TIMER
}

private enum class SettingsDialogFocusTarget {
    THEME_DARK,
    THEME_LIGHT,
    THEME_OLED,
    ACCENT_RED,
    ACCENT_BLUE,
    ACCENT_GREEN,
    ACCENT_PURPLE,
    HIGHLIGHT_DEFAULT,
    HIGHLIGHT_RED,
    HIGHLIGHT_BLUE,
    HIGHLIGHT_GREEN,
    HIGHLIGHT_PURPLE,
    CLOSE
}

private fun androidx.compose.ui.input.key.KeyEvent.toControllerInput(): ControllerInput? = when (nativeKeyEvent.keyCode) {
    AndroidKeyEvent.KEYCODE_DPAD_LEFT -> ControllerInput.LEFT
    AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> ControllerInput.RIGHT
    AndroidKeyEvent.KEYCODE_DPAD_UP -> ControllerInput.UP
    AndroidKeyEvent.KEYCODE_DPAD_DOWN -> ControllerInput.DOWN
    AndroidKeyEvent.KEYCODE_DPAD_CENTER,
    AndroidKeyEvent.KEYCODE_ENTER,
    AndroidKeyEvent.KEYCODE_NUMPAD_ENTER,
    AndroidKeyEvent.KEYCODE_BUTTON_A -> ControllerInput.PRIMARY
    AndroidKeyEvent.KEYCODE_BUTTON_B -> ControllerInput.BACK
    AndroidKeyEvent.KEYCODE_BUTTON_X -> ControllerInput.SECONDARY
    AndroidKeyEvent.KEYCODE_BUTTON_Y -> ControllerInput.TERTIARY
    else -> null
}

private fun DashboardFocusTarget.timerSlotIndexOrNull(): Int? = when (this) {
    DashboardFocusTarget.TIMER_0 -> 0
    DashboardFocusTarget.TIMER_1 -> 1
    DashboardFocusTarget.TIMER_2 -> 2
    DashboardFocusTarget.TIMER_3 -> 3
    else -> null
}

private fun DashboardFocusTarget.toTimerOrNull(visibleTimers: List<TimerItem>): TimerItem? {
    val slotIndex = timerSlotIndexOrNull() ?: return null
    return visibleTimers.getOrNull(slotIndex)
}

private fun DashboardFocusTarget.sanitize(pageCount: Int): DashboardFocusTarget {
    return if (pageCount <= 1 && (this == DashboardFocusTarget.PREVIOUS_PAGE || this == DashboardFocusTarget.NEXT_PAGE)) {
        DashboardFocusTarget.TIMER_2
    } else {
        this
    }
}

private fun DashboardFocusTarget.moveLeft(): DashboardFocusTarget = when (this) {
    DashboardFocusTarget.SETTINGS -> DashboardFocusTarget.PRESET_FIFTEEN
    DashboardFocusTarget.CUSTOM -> DashboardFocusTarget.CUSTOM
    DashboardFocusTarget.PRESET_FIVE -> DashboardFocusTarget.CUSTOM
    DashboardFocusTarget.PRESET_TEN -> DashboardFocusTarget.PRESET_FIVE
    DashboardFocusTarget.PRESET_FIFTEEN -> DashboardFocusTarget.PRESET_TEN
    DashboardFocusTarget.TIMER_0 -> DashboardFocusTarget.TIMER_0
    DashboardFocusTarget.TIMER_1 -> DashboardFocusTarget.TIMER_0
    DashboardFocusTarget.TIMER_2 -> DashboardFocusTarget.TIMER_2
    DashboardFocusTarget.TIMER_3 -> DashboardFocusTarget.TIMER_2
    DashboardFocusTarget.PREVIOUS_PAGE -> DashboardFocusTarget.PREVIOUS_PAGE
    DashboardFocusTarget.NEXT_PAGE -> DashboardFocusTarget.PREVIOUS_PAGE
}

private fun DashboardFocusTarget.moveRight(): DashboardFocusTarget = when (this) {
    DashboardFocusTarget.SETTINGS -> DashboardFocusTarget.SETTINGS
    DashboardFocusTarget.CUSTOM -> DashboardFocusTarget.PRESET_FIVE
    DashboardFocusTarget.PRESET_FIVE -> DashboardFocusTarget.PRESET_TEN
    DashboardFocusTarget.PRESET_TEN -> DashboardFocusTarget.PRESET_FIFTEEN
    DashboardFocusTarget.PRESET_FIFTEEN -> DashboardFocusTarget.SETTINGS
    DashboardFocusTarget.TIMER_0 -> DashboardFocusTarget.TIMER_1
    DashboardFocusTarget.TIMER_1 -> DashboardFocusTarget.TIMER_1
    DashboardFocusTarget.TIMER_2 -> DashboardFocusTarget.TIMER_3
    DashboardFocusTarget.TIMER_3 -> DashboardFocusTarget.TIMER_3
    DashboardFocusTarget.PREVIOUS_PAGE -> DashboardFocusTarget.NEXT_PAGE
    DashboardFocusTarget.NEXT_PAGE -> DashboardFocusTarget.NEXT_PAGE
}

private fun DashboardFocusTarget.moveUp(pageCount: Int): DashboardFocusTarget = when (this) {
    DashboardFocusTarget.SETTINGS -> DashboardFocusTarget.SETTINGS
    DashboardFocusTarget.CUSTOM -> DashboardFocusTarget.CUSTOM
    DashboardFocusTarget.PRESET_FIVE -> DashboardFocusTarget.PRESET_FIVE
    DashboardFocusTarget.PRESET_TEN -> DashboardFocusTarget.SETTINGS
    DashboardFocusTarget.PRESET_FIFTEEN -> DashboardFocusTarget.SETTINGS
    DashboardFocusTarget.TIMER_0 -> DashboardFocusTarget.CUSTOM
    DashboardFocusTarget.TIMER_1 -> DashboardFocusTarget.PRESET_TEN
    DashboardFocusTarget.TIMER_2 -> DashboardFocusTarget.TIMER_0
    DashboardFocusTarget.TIMER_3 -> DashboardFocusTarget.TIMER_1
    DashboardFocusTarget.PREVIOUS_PAGE -> DashboardFocusTarget.TIMER_2
    DashboardFocusTarget.NEXT_PAGE -> DashboardFocusTarget.TIMER_3
}.sanitize(pageCount)

private fun DashboardFocusTarget.moveDown(pageCount: Int): DashboardFocusTarget = when (this) {
    DashboardFocusTarget.SETTINGS -> DashboardFocusTarget.PRESET_FIFTEEN
    DashboardFocusTarget.CUSTOM,
    DashboardFocusTarget.PRESET_FIVE -> DashboardFocusTarget.TIMER_0
    DashboardFocusTarget.PRESET_TEN,
    DashboardFocusTarget.PRESET_FIFTEEN -> DashboardFocusTarget.TIMER_1
    DashboardFocusTarget.TIMER_0 -> DashboardFocusTarget.TIMER_2
    DashboardFocusTarget.TIMER_1 -> DashboardFocusTarget.TIMER_3
    DashboardFocusTarget.TIMER_2 -> if (pageCount > 1) DashboardFocusTarget.PREVIOUS_PAGE else DashboardFocusTarget.TIMER_2
    DashboardFocusTarget.TIMER_3 -> if (pageCount > 1) DashboardFocusTarget.NEXT_PAGE else DashboardFocusTarget.TIMER_3
    DashboardFocusTarget.PREVIOUS_PAGE -> DashboardFocusTarget.PREVIOUS_PAGE
    DashboardFocusTarget.NEXT_PAGE -> DashboardFocusTarget.NEXT_PAGE
}.sanitize(pageCount)

private fun BuilderDialogFocusTarget.moveLeft(): BuilderDialogFocusTarget = when (this) {
    BuilderDialogFocusTarget.DIGIT_2 -> BuilderDialogFocusTarget.DIGIT_1
    BuilderDialogFocusTarget.DIGIT_3 -> BuilderDialogFocusTarget.DIGIT_2
    BuilderDialogFocusTarget.DIGIT_5 -> BuilderDialogFocusTarget.DIGIT_4
    BuilderDialogFocusTarget.DIGIT_6 -> BuilderDialogFocusTarget.DIGIT_5
    BuilderDialogFocusTarget.DIGIT_8 -> BuilderDialogFocusTarget.DIGIT_7
    BuilderDialogFocusTarget.DIGIT_9 -> BuilderDialogFocusTarget.DIGIT_8
    BuilderDialogFocusTarget.DIGIT_0 -> BuilderDialogFocusTarget.CLEAR
    BuilderDialogFocusTarget.DELETE -> BuilderDialogFocusTarget.DIGIT_0
    BuilderDialogFocusTarget.ADD_TIMER -> BuilderDialogFocusTarget.CANCEL
    else -> this
}

private fun BuilderDialogFocusTarget.moveRight(): BuilderDialogFocusTarget = when (this) {
    BuilderDialogFocusTarget.DIGIT_1 -> BuilderDialogFocusTarget.DIGIT_2
    BuilderDialogFocusTarget.DIGIT_2 -> BuilderDialogFocusTarget.DIGIT_3
    BuilderDialogFocusTarget.DIGIT_4 -> BuilderDialogFocusTarget.DIGIT_5
    BuilderDialogFocusTarget.DIGIT_5 -> BuilderDialogFocusTarget.DIGIT_6
    BuilderDialogFocusTarget.DIGIT_7 -> BuilderDialogFocusTarget.DIGIT_8
    BuilderDialogFocusTarget.DIGIT_8 -> BuilderDialogFocusTarget.DIGIT_9
    BuilderDialogFocusTarget.CLEAR -> BuilderDialogFocusTarget.DIGIT_0
    BuilderDialogFocusTarget.DIGIT_0 -> BuilderDialogFocusTarget.DELETE
    BuilderDialogFocusTarget.CANCEL -> BuilderDialogFocusTarget.ADD_TIMER
    else -> this
}

private fun BuilderDialogFocusTarget.moveUp(): BuilderDialogFocusTarget = when (this) {
    BuilderDialogFocusTarget.DIGIT_4 -> BuilderDialogFocusTarget.DIGIT_1
    BuilderDialogFocusTarget.DIGIT_5 -> BuilderDialogFocusTarget.DIGIT_2
    BuilderDialogFocusTarget.DIGIT_6 -> BuilderDialogFocusTarget.DIGIT_3
    BuilderDialogFocusTarget.DIGIT_7 -> BuilderDialogFocusTarget.DIGIT_4
    BuilderDialogFocusTarget.DIGIT_8 -> BuilderDialogFocusTarget.DIGIT_5
    BuilderDialogFocusTarget.DIGIT_9 -> BuilderDialogFocusTarget.DIGIT_6
    BuilderDialogFocusTarget.CLEAR -> BuilderDialogFocusTarget.DIGIT_7
    BuilderDialogFocusTarget.DIGIT_0 -> BuilderDialogFocusTarget.DIGIT_8
    BuilderDialogFocusTarget.DELETE -> BuilderDialogFocusTarget.DIGIT_9
    BuilderDialogFocusTarget.CANCEL -> BuilderDialogFocusTarget.CLEAR
    BuilderDialogFocusTarget.ADD_TIMER -> BuilderDialogFocusTarget.DIGIT_0
    else -> this
}

private fun BuilderDialogFocusTarget.moveDown(): BuilderDialogFocusTarget = when (this) {
    BuilderDialogFocusTarget.DIGIT_1 -> BuilderDialogFocusTarget.DIGIT_4
    BuilderDialogFocusTarget.DIGIT_2 -> BuilderDialogFocusTarget.DIGIT_5
    BuilderDialogFocusTarget.DIGIT_3 -> BuilderDialogFocusTarget.DIGIT_6
    BuilderDialogFocusTarget.DIGIT_4 -> BuilderDialogFocusTarget.DIGIT_7
    BuilderDialogFocusTarget.DIGIT_5 -> BuilderDialogFocusTarget.DIGIT_8
    BuilderDialogFocusTarget.DIGIT_6 -> BuilderDialogFocusTarget.DIGIT_9
    BuilderDialogFocusTarget.DIGIT_7 -> BuilderDialogFocusTarget.CLEAR
    BuilderDialogFocusTarget.DIGIT_8 -> BuilderDialogFocusTarget.DIGIT_0
    BuilderDialogFocusTarget.DIGIT_9 -> BuilderDialogFocusTarget.DELETE
    BuilderDialogFocusTarget.CLEAR -> BuilderDialogFocusTarget.CANCEL
    BuilderDialogFocusTarget.DIGIT_0 -> BuilderDialogFocusTarget.ADD_TIMER
    BuilderDialogFocusTarget.DELETE -> BuilderDialogFocusTarget.ADD_TIMER
    else -> this
}

private fun SettingsDialogFocusTarget.moveLeft(): SettingsDialogFocusTarget = when (this) {
    SettingsDialogFocusTarget.THEME_LIGHT -> SettingsDialogFocusTarget.THEME_DARK
    SettingsDialogFocusTarget.THEME_OLED -> SettingsDialogFocusTarget.THEME_LIGHT
    SettingsDialogFocusTarget.ACCENT_BLUE -> SettingsDialogFocusTarget.ACCENT_RED
    SettingsDialogFocusTarget.ACCENT_GREEN -> SettingsDialogFocusTarget.ACCENT_BLUE
    SettingsDialogFocusTarget.ACCENT_PURPLE -> SettingsDialogFocusTarget.ACCENT_GREEN
    SettingsDialogFocusTarget.HIGHLIGHT_RED -> SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT
    SettingsDialogFocusTarget.HIGHLIGHT_BLUE -> SettingsDialogFocusTarget.HIGHLIGHT_RED
    SettingsDialogFocusTarget.HIGHLIGHT_GREEN -> SettingsDialogFocusTarget.HIGHLIGHT_BLUE
    SettingsDialogFocusTarget.HIGHLIGHT_PURPLE -> SettingsDialogFocusTarget.HIGHLIGHT_GREEN
    else -> this
}

private fun SettingsDialogFocusTarget.moveRight(): SettingsDialogFocusTarget = when (this) {
    SettingsDialogFocusTarget.THEME_DARK -> SettingsDialogFocusTarget.THEME_LIGHT
    SettingsDialogFocusTarget.THEME_LIGHT -> SettingsDialogFocusTarget.THEME_OLED
    SettingsDialogFocusTarget.ACCENT_RED -> SettingsDialogFocusTarget.ACCENT_BLUE
    SettingsDialogFocusTarget.ACCENT_BLUE -> SettingsDialogFocusTarget.ACCENT_GREEN
    SettingsDialogFocusTarget.ACCENT_GREEN -> SettingsDialogFocusTarget.ACCENT_PURPLE
    SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT -> SettingsDialogFocusTarget.HIGHLIGHT_RED
    SettingsDialogFocusTarget.HIGHLIGHT_RED -> SettingsDialogFocusTarget.HIGHLIGHT_BLUE
    SettingsDialogFocusTarget.HIGHLIGHT_BLUE -> SettingsDialogFocusTarget.HIGHLIGHT_GREEN
    SettingsDialogFocusTarget.HIGHLIGHT_GREEN -> SettingsDialogFocusTarget.HIGHLIGHT_PURPLE
    else -> this
}

private fun SettingsDialogFocusTarget.moveUp(): SettingsDialogFocusTarget = when (this) {
    SettingsDialogFocusTarget.ACCENT_RED -> SettingsDialogFocusTarget.THEME_DARK
    SettingsDialogFocusTarget.ACCENT_BLUE -> SettingsDialogFocusTarget.THEME_LIGHT
    SettingsDialogFocusTarget.ACCENT_GREEN,
    SettingsDialogFocusTarget.ACCENT_PURPLE -> SettingsDialogFocusTarget.THEME_OLED
    SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT,
    SettingsDialogFocusTarget.HIGHLIGHT_RED -> SettingsDialogFocusTarget.ACCENT_RED
    SettingsDialogFocusTarget.HIGHLIGHT_BLUE -> SettingsDialogFocusTarget.ACCENT_BLUE
    SettingsDialogFocusTarget.HIGHLIGHT_GREEN -> SettingsDialogFocusTarget.ACCENT_GREEN
    SettingsDialogFocusTarget.HIGHLIGHT_PURPLE -> SettingsDialogFocusTarget.ACCENT_PURPLE
    SettingsDialogFocusTarget.CLOSE -> SettingsDialogFocusTarget.HIGHLIGHT_BLUE
    else -> this
}

private fun SettingsDialogFocusTarget.moveDown(): SettingsDialogFocusTarget = when (this) {
    SettingsDialogFocusTarget.THEME_DARK -> SettingsDialogFocusTarget.ACCENT_RED
    SettingsDialogFocusTarget.THEME_LIGHT -> SettingsDialogFocusTarget.ACCENT_BLUE
    SettingsDialogFocusTarget.THEME_OLED -> SettingsDialogFocusTarget.ACCENT_GREEN
    SettingsDialogFocusTarget.ACCENT_RED -> SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT
    SettingsDialogFocusTarget.ACCENT_BLUE -> SettingsDialogFocusTarget.HIGHLIGHT_BLUE
    SettingsDialogFocusTarget.ACCENT_GREEN -> SettingsDialogFocusTarget.HIGHLIGHT_GREEN
    SettingsDialogFocusTarget.ACCENT_PURPLE -> SettingsDialogFocusTarget.HIGHLIGHT_PURPLE
    SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT,
    SettingsDialogFocusTarget.HIGHLIGHT_RED,
    SettingsDialogFocusTarget.HIGHLIGHT_BLUE,
    SettingsDialogFocusTarget.HIGHLIGHT_GREEN,
    SettingsDialogFocusTarget.HIGHLIGHT_PURPLE -> SettingsDialogFocusTarget.CLOSE
    SettingsDialogFocusTarget.CLOSE -> SettingsDialogFocusTarget.CLOSE
}

private fun AppThemeMode.toSettingsFocusTarget(): SettingsDialogFocusTarget = when (this) {
    AppThemeMode.DARK -> SettingsDialogFocusTarget.THEME_DARK
    AppThemeMode.LIGHT -> SettingsDialogFocusTarget.THEME_LIGHT
    AppThemeMode.OLED -> SettingsDialogFocusTarget.THEME_OLED
}

private fun String.toTimerInputSeconds(): Int {
    val normalized = takeLast(4).padStart(4, '0')
    val minutes = normalized.take(2).toIntOrNull() ?: 0
    val seconds = normalized.takeLast(2).toIntOrNull() ?: 0
    return minutes * 60 + seconds
}

private fun String.toTimerInputDisplay(): String {
    val normalized = takeLast(4).padStart(4, '0')
    return "${normalized.take(2)}:${normalized.takeLast(2)}"
}

private fun android.view.View.performControllerMoveHaptic() {
    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
}

private data class ControllerHint(
    val button: String,
    val label: String
)

private data class ControllerFocusTarget(
    val boundsInRoot: Rect,
    val shape: Shape
)

private data class ControllerFocusRegistrar(
    val onFocusTarget: (ControllerFocusTarget) -> Unit
)

private val LocalControllerFocusRegistrar = staticCompositionLocalOf<ControllerFocusRegistrar?> { null }
private val LocalControllerFocusColor = staticCompositionLocalOf { Color.Unspecified }

private fun dashboardControllerHints(
    focusedTarget: DashboardFocusTarget,
    visibleTimers: List<TimerItem>,
    currentPage: Int,
    pageCount: Int
): List<ControllerHint> {
    val baseHints = mutableListOf(
        ControllerHint(button = "D-pad", label = "Move")
    )

    when (focusedTarget) {
        DashboardFocusTarget.SETTINGS -> {
            baseHints += ControllerHint(button = "A", label = "Settings")
        }

        DashboardFocusTarget.CUSTOM -> {
            baseHints += ControllerHint(button = "A", label = "Custom")
        }

        DashboardFocusTarget.PRESET_FIVE,
        DashboardFocusTarget.PRESET_TEN,
        DashboardFocusTarget.PRESET_FIFTEEN -> {
            baseHints += ControllerHint(button = "A", label = "Add")
        }

        DashboardFocusTarget.TIMER_0,
        DashboardFocusTarget.TIMER_1,
        DashboardFocusTarget.TIMER_2,
        DashboardFocusTarget.TIMER_3 -> {
            val timer = focusedTarget.toTimerOrNull(visibleTimers)
            if (timer == null) {
                baseHints += ControllerHint(button = "A", label = "Custom")
            } else {
                baseHints += ControllerHint(
                    button = "A",
                    label = if (timer.isRunning) "Pause" else if (timer.remainingSeconds == 0) "Restart" else "Start"
                )
                baseHints += ControllerHint(button = "X", label = "Reset")
                baseHints += ControllerHint(button = "Y", label = "Remove")
            }
        }

        DashboardFocusTarget.PREVIOUS_PAGE -> {
            if (currentPage > 0) {
                baseHints += ControllerHint(button = "A", label = "Prev")
            }
        }

        DashboardFocusTarget.NEXT_PAGE -> {
            if (currentPage < pageCount - 1) {
                baseHints += ControllerHint(button = "A", label = "Next")
            }
        }
    }

    return baseHints
}

@Composable
fun TimerApp() {
    val context = LocalContext.current
    val appSettingsStore = remember(context) { AppSettingsStore(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    val appSettings by appSettingsStore.settings.collectAsState(initial = AppSettings())
    val timers = remember { mutableStateListOf<TimerItem>() }
    val finishedTimerIds = remember { mutableStateListOf<Int>() }
    var nextTimerId by rememberSaveable { mutableIntStateOf(1) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var isBuilderOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var activeAlertRingtone by remember { mutableStateOf<Ringtone?>(null) }
    val themeMode = appSettings.themeMode
    val accentColor = appSettings.accentColor
    val controllerHighlightColor = appSettings.controllerHighlightColor

    ThorMultiTimerTheme(themeMode = themeMode, accentColor = accentColor) {
        val controllerFocusColor = controllerHighlightColor.resolveControllerFocusColor(
            defaultColor = MaterialTheme.colorScheme.onSurface
        )

        CompositionLocalProvider(LocalControllerFocusColor provides controllerFocusColor) {
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
                    TimerItem(
                        id = nextTimerId,
                        totalSeconds = totalSeconds,
                        remainingSeconds = totalSeconds,
                        isRunning = startImmediately,
                        endTimeMillis = endTimeMillis
                    )
                )
                nextTimerId += 1
                currentPage = ((timers.size - 1) / TimersPerPage).coerceAtLeast(0)
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
                    selectedControllerHighlightColor = controllerHighlightColor,
                    onThemeSelected = { selectedMode ->
                        coroutineScope.launch {
                            appSettingsStore.setThemeMode(selectedMode)
                        }
                    },
                    onAccentSelected = { selectedAccent ->
                        coroutineScope.launch {
                            appSettingsStore.setAccentColor(selectedAccent)
                        }
                    },
                    onControllerHighlightSelected = { selectedHighlight ->
                        coroutineScope.launch {
                            appSettingsStore.setControllerHighlightColor(selectedHighlight)
                        }
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
    var focusedTargetName by rememberSaveable { mutableStateOf(DashboardFocusTarget.CUSTOM.name) }
    var isControllerMode by rememberSaveable { mutableStateOf(false) }
    var lastControllerInputNonce by rememberSaveable { mutableIntStateOf(0) }
    val view = LocalView.current
    val focusedTarget = DashboardFocusTarget.valueOf(focusedTargetName)
    val activeFocusedTarget = if (isControllerMode) focusedTarget else null
    val controllerHints = dashboardControllerHints(
        focusedTarget = focusedTarget,
        visibleTimers = visibleTimers,
        currentPage = currentPage,
        pageCount = pageCount
    )

    val openSettingsFromTouch = {
        isControllerMode = false
        onOpenSettings()
    }
    val openBuilderFromTouch = {
        isControllerMode = false
        onOpenBuilder()
    }
    val quickAddFromTouch: (Int) -> Unit = { seconds ->
        isControllerMode = false
        onQuickAdd(seconds)
    }
    val startPauseFromTouch: (Int) -> Unit = { timerId ->
        isControllerMode = false
        onStartPause(timerId)
    }
    val resetFromTouch: (Int) -> Unit = { timerId ->
        isControllerMode = false
        onReset(timerId)
    }
    val removeFromTouch: (Int) -> Unit = { timerId ->
        isControllerMode = false
        onRemove(timerId)
    }
    val previousPageFromTouch = {
        isControllerMode = false
        onPreviousPage()
    }
    val nextPageFromTouch = {
        isControllerMode = false
        onNextPage()
    }

    LaunchedEffect(pageCount) {
        focusedTargetName = focusedTarget.sanitize(pageCount).name
    }

    LaunchedEffect(isControllerMode, lastControllerInputNonce) {
        if (!isControllerMode) return@LaunchedEffect
        delay(10_000)
        isControllerMode = false
    }

    ControllerInputBox(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        showControllerFocusIndicator = isControllerMode,
        onControllerInput = { input ->
            isControllerMode = true
            lastControllerInputNonce += 1
            when (input) {
                ControllerInput.LEFT -> {
                    val nextTarget = focusedTarget.moveLeft()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.RIGHT -> {
                    val nextTarget = focusedTarget.moveRight()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.UP -> {
                    val nextTarget = focusedTarget.moveUp(pageCount)
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.DOWN -> {
                    val nextTarget = focusedTarget.moveDown(pageCount)
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.PRIMARY -> {
                    when (focusedTarget) {
                        DashboardFocusTarget.SETTINGS -> onOpenSettings()
                        DashboardFocusTarget.CUSTOM -> onOpenBuilder()
                        DashboardFocusTarget.PRESET_FIVE -> onQuickAdd(300)
                        DashboardFocusTarget.PRESET_TEN -> onQuickAdd(600)
                        DashboardFocusTarget.PRESET_FIFTEEN -> onQuickAdd(900)

                        DashboardFocusTarget.TIMER_0,
                        DashboardFocusTarget.TIMER_1,
                        DashboardFocusTarget.TIMER_2,
                        DashboardFocusTarget.TIMER_3 -> {
                            val timer = focusedTarget.toTimerOrNull(visibleTimers)
                            if (timer != null) {
                                onStartPause(timer.id)
                            } else {
                                onOpenBuilder()
                            }
                        }

                        DashboardFocusTarget.PREVIOUS_PAGE -> onPreviousPage()
                        DashboardFocusTarget.NEXT_PAGE -> onNextPage()
                    }
                    true
                }

                ControllerInput.BACK -> true

                ControllerInput.SECONDARY -> {
                    focusedTarget.toTimerOrNull(visibleTimers)?.let { onReset(it.id) }
                    true
                }

                ControllerInput.TERTIARY -> {
                    focusedTarget.toTimerOrNull(visibleTimers)?.let { onRemove(it.id) }
                    true
                }
            }
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 220)),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeaderSection(
                onOpenSettings = openSettingsFromTouch,
                isSettingsFocused = activeFocusedTarget == DashboardFocusTarget.SETTINGS
            )
            ActionSection(
                onOpenBuilder = openBuilderFromTouch,
                onQuickAdd = quickAddFromTouch,
                focusedTarget = activeFocusedTarget
            )
            TimerGrid(
                timers = visibleTimers,
                focusedTarget = activeFocusedTarget,
                onStartPause = startPauseFromTouch,
                onReset = resetFromTouch,
                onRemove = removeFromTouch,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 220))
            )
            if (pageCount > 1) {
                PageControls(
                    currentPage = currentPage,
                    pageCount = pageCount,
                    canGoBack = currentPage > 0,
                    canGoForward = currentPage < pageCount - 1,
                    focusedTarget = activeFocusedTarget,
                    onPreviousPage = previousPageFromTouch,
                    onNextPage = nextPageFromTouch
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = isControllerMode,
                enter = fadeIn(animationSpec = tween(durationMillis = 160)) +
                    expandVertically(animationSpec = tween(durationMillis = 220)),
                exit = fadeOut(animationSpec = tween(durationMillis = 140)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 220))
            ) {
                ControllerHintBar(
                    hints = controllerHints,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    onOpenSettings: () -> Unit,
    isSettingsFocused: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Thor MultiTimer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        AnimatedOutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .width(50.dp)
                .height(46.dp),
            controllerFocused = isSettingsFocused,
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
    onQuickAdd: (Int) -> Unit,
    focusedTarget: DashboardFocusTarget?
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedButton(
            onClick = onOpenBuilder,
            modifier = Modifier
                .weight(1.3f)
                .heightIn(min = 56.dp),
            controllerFocused = focusedTarget == DashboardFocusTarget.CUSTOM,
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(text = "Custom", style = MaterialTheme.typography.titleMedium)
        }

        QuickAddButton(
            label = "5m",
            modifier = Modifier.weight(1f),
            isControllerFocused = focusedTarget == DashboardFocusTarget.PRESET_FIVE,
            onClick = { onQuickAdd(300) }
        )
        QuickAddButton(
            label = "10m",
            modifier = Modifier.weight(1f),
            isControllerFocused = focusedTarget == DashboardFocusTarget.PRESET_TEN,
            onClick = { onQuickAdd(600) }
        )
        QuickAddButton(
            label = "15m",
            modifier = Modifier.weight(1f),
            isControllerFocused = focusedTarget == DashboardFocusTarget.PRESET_FIFTEEN,
            onClick = { onQuickAdd(900) }
        )
    }
}

@Composable
private fun QuickAddButton(
    label: String,
    modifier: Modifier = Modifier,
    isControllerFocused: Boolean = false,
    onClick: () -> Unit
) {
    AnimatedOutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        controllerFocused = isControllerFocused,
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TimerGrid(
    timers: List<TimerItem>,
    focusedTarget: DashboardFocusTarget?,
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        slots.chunked(2).forEachIndexed { rowIndex, rowTimers ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowTimers.forEachIndexed { columnIndex, timer ->
                    val slotIndex = rowIndex * 2 + columnIndex
                    TimerSlot(
                        timer = timer,
                        isControllerFocused = focusedTarget?.timerSlotIndexOrNull() == slotIndex,
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
    isControllerFocused: Boolean,
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
        BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.9f)
        )
    } else {
        null
    }

    Card(
        modifier = modifier.controllerFocusOutline(
            isFocused = isControllerFocused,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = slotColor),
        border = slotBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isEmptySlot) {
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
                                .width(34.dp)
                                .height(34.dp),
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
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(end = 44.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = formatClock(timer.remainingSeconds),
                                    style = TextStyle(
                                        fontSize = 58.sp,
                                        lineHeight = 58.sp,
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AnimatedButton(
                                        onClick = { onStartPause(timer.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 42.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Text("Pause", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    }
                                    AnimatedOutlinedButton(
                                        onClick = { onReset(timer.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 42.dp),
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
                                        .heightIn(min = 42.dp),
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
    focusedTarget: DashboardFocusTarget?,
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
            controllerFocused = focusedTarget == DashboardFocusTarget.PREVIOUS_PAGE,
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
            controllerFocused = focusedTarget == DashboardFocusTarget.NEXT_PAGE,
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun ControllerHintBar(
    hints: List<ControllerHint>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hints.forEach { hint ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hint.button,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                Text(
                    text = hint.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TimerBuilderDialog(
    onDismiss: () -> Unit,
    onAddTimer: (Int) -> Unit
) {
    var timerInput by rememberSaveable { mutableStateOf("") }
    var focusedTargetName by rememberSaveable { mutableStateOf(BuilderDialogFocusTarget.DIGIT_1.name) }
    val view = LocalView.current
    val focusedTarget = BuilderDialogFocusTarget.valueOf(focusedTargetName)
    val totalSeconds = timerInput.toTimerInputSeconds()
    val canAddTimer = totalSeconds > 0

    fun appendDigit(digit: Int) {
        timerInput = (timerInput + digit.toString()).takeLast(4)
    }

    AppDialog(
        onDismiss = onDismiss,
        onControllerInput = { input ->
            when (input) {
                ControllerInput.LEFT -> {
                    val nextTarget = focusedTarget.moveLeft()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.RIGHT -> {
                    val nextTarget = focusedTarget.moveRight()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.UP -> {
                    val nextTarget = focusedTarget.moveUp()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.DOWN -> {
                    val nextTarget = focusedTarget.moveDown()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.PRIMARY -> {
                    when (focusedTarget) {
                        BuilderDialogFocusTarget.DIGIT_1 -> appendDigit(1)
                        BuilderDialogFocusTarget.DIGIT_2 -> appendDigit(2)
                        BuilderDialogFocusTarget.DIGIT_3 -> appendDigit(3)
                        BuilderDialogFocusTarget.DIGIT_4 -> appendDigit(4)
                        BuilderDialogFocusTarget.DIGIT_5 -> appendDigit(5)
                        BuilderDialogFocusTarget.DIGIT_6 -> appendDigit(6)
                        BuilderDialogFocusTarget.DIGIT_7 -> appendDigit(7)
                        BuilderDialogFocusTarget.DIGIT_8 -> appendDigit(8)
                        BuilderDialogFocusTarget.DIGIT_9 -> appendDigit(9)
                        BuilderDialogFocusTarget.CLEAR -> timerInput = ""
                        BuilderDialogFocusTarget.DIGIT_0 -> appendDigit(0)
                        BuilderDialogFocusTarget.DELETE -> timerInput = timerInput.dropLast(1)
                        BuilderDialogFocusTarget.CANCEL -> onDismiss()
                        BuilderDialogFocusTarget.ADD_TIMER -> if (canAddTimer) {
                            onAddTimer(totalSeconds)
                        }
                    }
                    true
                }

                ControllerInput.BACK -> true

                ControllerInput.SECONDARY -> true

                ControllerInput.TERTIARY -> {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDismiss()
                    true
                }
            }
        }
    ) {
        DialogCard(
            title = "New Timer",
            subtitle = "",
            compact = true,
            headerAction = { PanelCloseIndicator() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timerInput.toTimerInputDisplay(),
                    style = TextStyle(
                        fontSize = 32.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BuilderKeypad(
                focusedTarget = focusedTarget,
                onDigit = ::appendDigit,
                onClear = { timerInput = "" },
                onDelete = { timerInput = timerInput.dropLast(1) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedTextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp),
                    controllerFocused = focusedTarget == BuilderDialogFocusTarget.CANCEL,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Cancel")
                }

                AnimatedButton(
                    onClick = { onAddTimer(totalSeconds) },
                    enabled = canAddTimer,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp),
                    controllerFocused = focusedTarget == BuilderDialogFocusTarget.ADD_TIMER,
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
    selectedControllerHighlightColor: ControllerHighlightColor,
    onThemeSelected: (AppThemeMode) -> Unit,
    onAccentSelected: (AppAccentColor) -> Unit,
    onControllerHighlightSelected: (ControllerHighlightColor) -> Unit,
    onDismiss: () -> Unit
) {
    var focusedTargetName by rememberSaveable { mutableStateOf(selectedThemeMode.toSettingsFocusTarget().name) }
    val view = LocalView.current
    val focusedTarget = SettingsDialogFocusTarget.valueOf(focusedTargetName)

    AppDialog(
        onDismiss = onDismiss,
        onControllerInput = { input ->
            when (input) {
                ControllerInput.LEFT -> {
                    val nextTarget = focusedTarget.moveLeft()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.RIGHT -> {
                    val nextTarget = focusedTarget.moveRight()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.UP -> {
                    val nextTarget = focusedTarget.moveUp()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.DOWN -> {
                    val nextTarget = focusedTarget.moveDown()
                    if (nextTarget != focusedTarget) {
                        view.performControllerMoveHaptic()
                    }
                    focusedTargetName = nextTarget.name
                    true
                }

                ControllerInput.PRIMARY -> {
                    when (focusedTarget) {
                        SettingsDialogFocusTarget.THEME_DARK -> onThemeSelected(AppThemeMode.DARK)
                        SettingsDialogFocusTarget.THEME_LIGHT -> onThemeSelected(AppThemeMode.LIGHT)
                        SettingsDialogFocusTarget.THEME_OLED -> onThemeSelected(AppThemeMode.OLED)
                        SettingsDialogFocusTarget.ACCENT_RED -> onAccentSelected(AppAccentColor.RED)
                        SettingsDialogFocusTarget.ACCENT_BLUE -> onAccentSelected(AppAccentColor.BLUE)
                        SettingsDialogFocusTarget.ACCENT_GREEN -> onAccentSelected(AppAccentColor.GREEN)
                        SettingsDialogFocusTarget.ACCENT_PURPLE -> onAccentSelected(AppAccentColor.PURPLE)
                        SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT -> onControllerHighlightSelected(ControllerHighlightColor.DEFAULT)
                        SettingsDialogFocusTarget.HIGHLIGHT_RED -> onControllerHighlightSelected(ControllerHighlightColor.RED)
                        SettingsDialogFocusTarget.HIGHLIGHT_BLUE -> onControllerHighlightSelected(ControllerHighlightColor.BLUE)
                        SettingsDialogFocusTarget.HIGHLIGHT_GREEN -> onControllerHighlightSelected(ControllerHighlightColor.GREEN)
                        SettingsDialogFocusTarget.HIGHLIGHT_PURPLE -> onControllerHighlightSelected(ControllerHighlightColor.PURPLE)
                        SettingsDialogFocusTarget.CLOSE -> onDismiss()
                    }
                    true
                }

                ControllerInput.BACK -> true

                ControllerInput.SECONDARY -> true

                ControllerInput.TERTIARY -> {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDismiss()
                    true
                }
            }
        }
    ) {
        DialogCard(
            title = "Settings",
            subtitle = "",
            headerAction = { PanelCloseIndicator() }
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ThemeModeToggle(
                selectedThemeMode = selectedThemeMode,
                focusedTarget = focusedTarget,
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
                focusedTarget = focusedTarget,
                onAccentSelected = onAccentSelected
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Highlight",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ControllerHighlightColorToggle(
                selectedHighlightColor = selectedControllerHighlightColor,
                focusedTarget = focusedTarget,
                onHighlightSelected = onControllerHighlightSelected
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp),
                controllerFocused = focusedTarget == SettingsDialogFocusTarget.CLOSE,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun BuilderKeypad(
    focusedTarget: BuilderDialogFocusTarget,
    onDigit: (Int) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("1", focusedTarget == BuilderDialogFocusTarget.DIGIT_1, Modifier.weight(1f)) { onDigit(1) }
            KeypadButton("2", focusedTarget == BuilderDialogFocusTarget.DIGIT_2, Modifier.weight(1f)) { onDigit(2) }
            KeypadButton("3", focusedTarget == BuilderDialogFocusTarget.DIGIT_3, Modifier.weight(1f)) { onDigit(3) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("4", focusedTarget == BuilderDialogFocusTarget.DIGIT_4, Modifier.weight(1f)) { onDigit(4) }
            KeypadButton("5", focusedTarget == BuilderDialogFocusTarget.DIGIT_5, Modifier.weight(1f)) { onDigit(5) }
            KeypadButton("6", focusedTarget == BuilderDialogFocusTarget.DIGIT_6, Modifier.weight(1f)) { onDigit(6) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("7", focusedTarget == BuilderDialogFocusTarget.DIGIT_7, Modifier.weight(1f)) { onDigit(7) }
            KeypadButton("8", focusedTarget == BuilderDialogFocusTarget.DIGIT_8, Modifier.weight(1f)) { onDigit(8) }
            KeypadButton("9", focusedTarget == BuilderDialogFocusTarget.DIGIT_9, Modifier.weight(1f)) { onDigit(9) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("Clear", focusedTarget == BuilderDialogFocusTarget.CLEAR, Modifier.weight(1f), textStyle = MaterialTheme.typography.labelMedium, outlined = true, onClick = onClear)
            KeypadButton("0", focusedTarget == BuilderDialogFocusTarget.DIGIT_0, Modifier.weight(1f)) { onDigit(0) }
            KeypadButton("Del", focusedTarget == BuilderDialogFocusTarget.DELETE, Modifier.weight(1f), textStyle = MaterialTheme.typography.labelMedium, outlined = true, onClick = onDelete)
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    controllerFocused: Boolean,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    if (outlined) {
        AnimatedOutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 44.dp),
            controllerFocused = controllerFocused,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(label, style = textStyle, maxLines = 1)
        }
    } else {
        AnimatedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 44.dp),
            controllerFocused = controllerFocused,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(label, style = textStyle, maxLines = 1)
        }
    }
}

@Composable
private fun FinishedTimerDialog(
    onStopAlert: () -> Unit
) {
    AppDialog(
        onDismiss = onStopAlert,
        onControllerInput = { input ->
            when (input) {
                ControllerInput.PRIMARY,
                ControllerInput.BACK -> {
                    onStopAlert()
                    true
                }

                ControllerInput.LEFT,
                ControllerInput.RIGHT,
                ControllerInput.UP,
                ControllerInput.DOWN,
                ControllerInput.SECONDARY,
                ControllerInput.TERTIARY -> true
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Timer Finished",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                AnimatedButton(
                    onClick = onStopAlert,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    controllerFocused = true,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Stop Alert",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentColorToggle(
    selectedAccentColor: AppAccentColor,
    focusedTarget: SettingsDialogFocusTarget,
    onAccentSelected: (AppAccentColor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AccentColorButton(
            accentColor = AppAccentColor.RED,
            isSelected = selectedAccentColor == AppAccentColor.RED,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.ACCENT_RED,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.RED) }
        )
        AccentColorButton(
            accentColor = AppAccentColor.BLUE,
            isSelected = selectedAccentColor == AppAccentColor.BLUE,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.ACCENT_BLUE,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.BLUE) }
        )
        AccentColorButton(
            accentColor = AppAccentColor.GREEN,
            isSelected = selectedAccentColor == AppAccentColor.GREEN,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.ACCENT_GREEN,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.GREEN) }
        )
        AccentColorButton(
            accentColor = AppAccentColor.PURPLE,
            isSelected = selectedAccentColor == AppAccentColor.PURPLE,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.ACCENT_PURPLE,
            modifier = Modifier.weight(1f),
            onClick = { onAccentSelected(AppAccentColor.PURPLE) }
        )
    }
}

@Composable
private fun AccentColorButton(
    accentColor: AppAccentColor,
    isSelected: Boolean,
    isControllerFocused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current

    Box(
        modifier = modifier
            .height(SettingsColorButtonMinHeight)
            .pressScale(interactionSource, pressedScale = 0.94f)
            .controllerFocusOutline(
                isFocused = isControllerFocused,
                shape = shape,
                color = MaterialTheme.colorScheme.onSurface
            )
            .clip(shape)
            .background(accentSwatchColor(accentColor), shape)
            .border(
                width = when {
                    isSelected -> 3.dp
                    else -> 1.5.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
                },
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            )
    )
}

private fun accentSwatchColor(accentColor: AppAccentColor): Color = when (accentColor) {
    AppAccentColor.RED -> RedAccent
    AppAccentColor.BLUE -> BlueAccent
    AppAccentColor.GREEN -> GreenAccent
    AppAccentColor.PURPLE -> PurpleAccent
}

private fun ControllerHighlightColor.resolveControllerFocusColor(defaultColor: Color): Color = when (this) {
    ControllerHighlightColor.DEFAULT -> defaultColor
    ControllerHighlightColor.RED -> RedAccent
    ControllerHighlightColor.BLUE -> BlueAccent
    ControllerHighlightColor.GREEN -> GreenAccent
    ControllerHighlightColor.PURPLE -> PurpleAccent
}

@Composable
private fun ThemeModeToggle(
    selectedThemeMode: AppThemeMode,
    focusedTarget: SettingsDialogFocusTarget,
    onThemeSelected: (AppThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeModeButton(
            label = "Dark",
            isSelected = selectedThemeMode == AppThemeMode.DARK,
            controllerFocused = focusedTarget == SettingsDialogFocusTarget.THEME_DARK,
            modifier = Modifier.weight(1f),
            onClick = { onThemeSelected(AppThemeMode.DARK) }
        )
        ThemeModeButton(
            label = "Light",
            isSelected = selectedThemeMode == AppThemeMode.LIGHT,
            controllerFocused = focusedTarget == SettingsDialogFocusTarget.THEME_LIGHT,
            modifier = Modifier.weight(1f),
            onClick = { onThemeSelected(AppThemeMode.LIGHT) }
        )
        ThemeModeButton(
            label = "OLED",
            isSelected = selectedThemeMode == AppThemeMode.OLED,
            controllerFocused = focusedTarget == SettingsDialogFocusTarget.THEME_OLED,
            modifier = Modifier.weight(1f),
            onClick = { onThemeSelected(AppThemeMode.OLED) }
        )
    }
}

@Composable
private fun ControllerHighlightColorToggle(
    selectedHighlightColor: ControllerHighlightColor,
    focusedTarget: SettingsDialogFocusTarget,
    onHighlightSelected: (ControllerHighlightColor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ControllerHighlightColorButton(
            highlightColor = ControllerHighlightColor.DEFAULT,
            isSelected = selectedHighlightColor == ControllerHighlightColor.DEFAULT,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.HIGHLIGHT_DEFAULT,
            modifier = Modifier.weight(1f),
            onClick = { onHighlightSelected(ControllerHighlightColor.DEFAULT) }
        )
        ControllerHighlightColorButton(
            highlightColor = ControllerHighlightColor.RED,
            isSelected = selectedHighlightColor == ControllerHighlightColor.RED,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.HIGHLIGHT_RED,
            modifier = Modifier.weight(1f),
            onClick = { onHighlightSelected(ControllerHighlightColor.RED) }
        )
        ControllerHighlightColorButton(
            highlightColor = ControllerHighlightColor.BLUE,
            isSelected = selectedHighlightColor == ControllerHighlightColor.BLUE,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.HIGHLIGHT_BLUE,
            modifier = Modifier.weight(1f),
            onClick = { onHighlightSelected(ControllerHighlightColor.BLUE) }
        )
        ControllerHighlightColorButton(
            highlightColor = ControllerHighlightColor.GREEN,
            isSelected = selectedHighlightColor == ControllerHighlightColor.GREEN,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.HIGHLIGHT_GREEN,
            modifier = Modifier.weight(1f),
            onClick = { onHighlightSelected(ControllerHighlightColor.GREEN) }
        )
        ControllerHighlightColorButton(
            highlightColor = ControllerHighlightColor.PURPLE,
            isSelected = selectedHighlightColor == ControllerHighlightColor.PURPLE,
            isControllerFocused = focusedTarget == SettingsDialogFocusTarget.HIGHLIGHT_PURPLE,
            modifier = Modifier.weight(1f),
            onClick = { onHighlightSelected(ControllerHighlightColor.PURPLE) }
        )
    }
}

@Composable
private fun ControllerHighlightColorButton(
    highlightColor: ControllerHighlightColor,
    isSelected: Boolean,
    isControllerFocused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current

    Box(
        modifier = modifier
            .height(SettingsColorButtonMinHeight)
            .pressScale(interactionSource, pressedScale = 0.94f)
            .controllerFocusOutline(
                isFocused = isControllerFocused,
                shape = shape,
                color = MaterialTheme.colorScheme.onSurface
            )
            .clip(shape)
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
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (highlightColor == ControllerHighlightColor.DEFAULT) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = highlightColor.resolveControllerFocusColor(MaterialTheme.colorScheme.onSurface),
                        shape = shape
                    )
            )
        }
    }
}

@Composable
private fun ThemeModeButton(
    label: String,
    isSelected: Boolean,
    controllerFocused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (isSelected) {
        AnimatedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 40.dp),
            controllerFocused = controllerFocused,
            focusRingInset = 1.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    } else {
        AnimatedOutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 40.dp),
            controllerFocused = controllerFocused,
            focusRingInset = 1.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun AppDialog(
    onDismiss: () -> Unit,
    onControllerInput: ((ControllerInput) -> Boolean)? = null,
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
        val dialogModifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
            .padding(14.dp)

        if (onControllerInput != null) {
            ControllerInputBox(
                modifier = dialogModifier,
                onControllerInput = onControllerInput,
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        } else {
            Box(
                modifier = dialogModifier,
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DialogCard(
    title: String,
    subtitle: String,
    compact: Boolean = false,
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(if (compact) 12.dp else 14.dp)) {
            if (title.isNotBlank() || headerAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    headerAction?.invoke()
                }
            }
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
            } else if (title.isNotBlank()) {
                Spacer(modifier = Modifier.height(if (compact) 6.dp else 8.dp))
            }
            content()
        }
    }
}

@Composable
private fun PanelCloseIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 7.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Y",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
        }
        Text(
            text = "Close",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseFocused: Boolean = false,
    increaseFocused: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            text = "-",
            controllerFocused = decreaseFocused,
            onClick = onDecrease
        )

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

        StepperButton(
            text = "+",
            controllerFocused = increaseFocused,
            onClick = onIncrease
        )
    }
}

@Composable
private fun CompactStepperRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseFocused: Boolean = false,
    increaseFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactStepperButton(
            text = "-",
            controllerFocused = decreaseFocused,
            onClick = onDecrease
        )
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
        CompactStepperButton(
            text = "+",
            controllerFocused = increaseFocused,
            onClick = onIncrease
        )
    }
}

@Composable
private fun StepperButton(
    text: String,
    controllerFocused: Boolean = false,
    onClick: () -> Unit
) {
    AnimatedOutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(64.dp)
            .heightIn(min = 56.dp),
        controllerFocused = controllerFocused,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun CompactStepperButton(
    text: String,
    controllerFocused: Boolean = false,
    onClick: () -> Unit
) {
    AnimatedOutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(56.dp)
            .heightIn(min = 52.dp),
        controllerFocused = controllerFocused,
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
    controllerFocused: Boolean = false,
    focusRingInset: androidx.compose.ui.unit.Dp = 0.dp,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = modifier
            .controllerFocusOutline(
                isFocused = controllerFocused,
                shape = shape,
                color = MaterialTheme.colorScheme.onSurface,
                inset = focusRingInset
            )
            .pressScale(interactionSource),
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
    controllerFocused: Boolean = false,
    focusRingInset: androidx.compose.ui.unit.Dp = 0.dp,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    OutlinedButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = modifier
            .controllerFocusOutline(
                isFocused = controllerFocused,
                shape = shape,
                color = MaterialTheme.colorScheme.onSurface,
                inset = focusRingInset
            )
            .pressScale(interactionSource),
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
    controllerFocused: Boolean = false,
    focusRingInset: androidx.compose.ui.unit.Dp = 0.dp,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    TextButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = modifier
            .controllerFocusOutline(
                isFocused = controllerFocused,
                shape = shape,
                color = MaterialTheme.colorScheme.onSurface,
                inset = focusRingInset
            )
            .pressScale(interactionSource),
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun ControllerInputBox(
    onControllerInput: (ControllerInput) -> Boolean,
    modifier: Modifier = Modifier,
    showControllerFocusIndicator: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val view = LocalView.current
    val density = LocalDensity.current
    val localFocusColor = LocalControllerFocusColor.current
    val focusColor = if (localFocusColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        localFocusColor
    }
    var containerBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var focusedTarget by remember { mutableStateOf<ControllerFocusTarget?>(null) }

    SideEffect {
        focusRequester.requestFocus()
    }

    LaunchedEffect(showControllerFocusIndicator) {
        if (!showControllerFocusIndicator) {
            delay(120)
            if (!showControllerFocusIndicator) {
                focusedTarget = null
            }
        }
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onGloballyPositioned { coordinates ->
                containerBoundsInRoot = coordinates.boundsInRoot()
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                val controllerInput = keyEvent.toControllerInput() ?: return@onPreviewKeyEvent false
                val handled = onControllerInput(controllerInput)

                if (handled && controllerInput == ControllerInput.PRIMARY) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }

                handled
            },
        contentAlignment = contentAlignment,
    ) {
        CompositionLocalProvider(
            LocalControllerFocusRegistrar provides ControllerFocusRegistrar(
                onFocusTarget = { target ->
                    focusedTarget = target
                }
            )
        ) {
            content()
        }

        val targetBounds = focusedTarget?.boundsInRoot
        val rootBounds = containerBoundsInRoot
        val targetShape = focusedTarget?.shape

        if (targetBounds != null && rootBounds != null && targetShape != null) {
            val focusMotionSpec = spring<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            val targetX = targetBounds.left - rootBounds.left
            val targetY = targetBounds.top - rootBounds.top
            val animatedX by animateFloatAsState(
                targetValue = targetX,
                animationSpec = focusMotionSpec,
                label = "controller_focus_x"
            )
            val animatedY by animateFloatAsState(
                targetValue = targetY,
                animationSpec = focusMotionSpec,
                label = "controller_focus_y"
            )
            val animatedWidth by animateFloatAsState(
                targetValue = targetBounds.width,
                animationSpec = focusMotionSpec,
                label = "controller_focus_width"
            )
            val animatedHeight by animateFloatAsState(
                targetValue = targetBounds.height,
                animationSpec = focusMotionSpec,
                label = "controller_focus_height"
            )
            val indicatorAlpha by animateFloatAsState(
                targetValue = if (showControllerFocusIndicator) 1f else 0f,
                animationSpec = tween(durationMillis = 120),
                label = "controller_focus_alpha"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(with(density) { animatedWidth.toDp() })
                    .height(with(density) { animatedHeight.toDp() })
                    .graphicsLayer {
                        translationX = animatedX
                        translationY = animatedY
                        alpha = indicatorAlpha
                    }
                    .border(3.dp, focusColor, targetShape)
            )
        }
    }
}

private fun Modifier.controllerFocusOutline(
    isFocused: Boolean,
    shape: Shape,
    color: Color,
    inset: androidx.compose.ui.unit.Dp = 0.dp
): Modifier {
    return composed {
        val registrar = LocalControllerFocusRegistrar.current
        val density = LocalDensity.current
        val localFocusColor = LocalControllerFocusColor.current
        val resolvedColor = if (localFocusColor == Color.Unspecified) color else localFocusColor
        var boundsInRoot by remember { mutableStateOf<Rect?>(null) }
        val insetPx = with(density) { inset.toPx() }

        fun Rect.adjustedForInset(): Rect {
            return Rect(
                left = left + insetPx,
                top = top + insetPx,
                right = right - insetPx,
                bottom = bottom - insetPx
            )
        }

        SideEffect {
            if (isFocused) {
                val targetBounds = boundsInRoot
                if (registrar != null && targetBounds != null) {
                    registrar.onFocusTarget(
                        ControllerFocusTarget(
                            boundsInRoot = targetBounds.adjustedForInset(),
                            shape = shape
                        )
                    )
                }
            }
        }

        when {
            registrar != null -> this.onGloballyPositioned { coordinates ->
                val targetBounds = coordinates.boundsInRoot()
                boundsInRoot = targetBounds
                if (isFocused) {
                    registrar.onFocusTarget(
                        ControllerFocusTarget(
                            boundsInRoot = targetBounds.adjustedForInset(),
                            shape = shape
                        )
                    )
                }
            }

            isFocused -> this.border(3.dp, resolvedColor, shape)
            else -> this
        }
    }
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
    ThorMultiTimerTheme {
        TimerApp()
    }
}
