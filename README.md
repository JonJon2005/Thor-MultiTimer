# FirstApp

`FirstApp` is a compact multi-timer Android app built for the AYN Thor bottom screen. It is written in Kotlin with Jetpack Compose and Material 3, and is intentionally designed around a small `1080 x 1240` portrait display with large controls, low clutter, and no scroll-dependent main workflow.

## What It Does

- Creates multiple timers quickly from one dashboard
- Starts preset timers immediately with `5m`, `10m`, and `15m`
- Supports custom timers through a compact popup
- Shows up to four timers at once in a `2 x 2` grid
- Supports `Start`, `Pause`, `Reset`, and `Remove`
- Plays the device system alarm or notification sound when a timer finishes
- Shows a large finish-alert panel with a big `Stop Alert` button
- Supports touch haptics and controller-confirm haptics
- Supports Thor hardware D-pad and face-button navigation

## Target Device

This app is being built specifically for the AYN Thor bottom screen.

Design assumptions:
- Single-screen app behavior only
- Bottom-screen target resolution: `1080 x 1240`
- Compact portrait layout
- Large tap targets
- No scroll-heavy UI
- No dual-screen or spanning APIs

## Current Features

### Dashboard

- Header with `Multi Timer` title and settings gear
- Quick actions row:
  - `Custom`
  - `5m`
  - `10m`
  - `15m`
- `2 x 2` timer card grid
- Page controls when more than four timers exist

### Timer Behavior

- Preset timers auto-start
- Custom timers are created paused
- Running timers update in real time
- Timer cards show only the most important information:
  - remaining time
  - stop/start actions
  - reset
  - remove

### Settings

- Theme modes:
  - `Dark`
  - `Light`
  - `OLED`
- Accent colors:
  - `Red`
  - `Blue`
  - `Green`
  - `Purple`

### Alerts

- Uses Android system alarm sound when available
- Falls back to system notification sound
- Large finish popup with oversized stop button

### Controller Support

The app supports Thor hardware buttons through explicit key handling.

- D-pad moves through logical UI targets
- `A` activates the focused control
- `B` dismisses dialogs / stops the finish alert
- `X` resets a focused timer card
- `Y` removes a focused timer card

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL
- Android minSdk `29`
- targetSdk `36`

## Project Structure

- [`MainActivity.kt`](/Users/jonathangallo/ANDROID_REPOS/firstapp/app/src/main/java/com/example/firstapp/MainActivity.kt): main UI, state, timer logic, dialogs, controller handling
- [`Theme.kt`](/Users/jonathangallo/ANDROID_REPOS/firstapp/app/src/main/java/com/example/firstapp/ui/theme/Theme.kt): theme composition and theme modes
- [`Color.kt`](/Users/jonathangallo/ANDROID_REPOS/firstapp/app/src/main/java/com/example/firstapp/ui/theme/Color.kt): palette and accent definitions
- [`settings.xml`](/Users/jonathangallo/ANDROID_REPOS/firstapp/app/src/main/res/drawable/settings.xml): settings gear drawable
- [`PROJECT_CONTEXT.txt`](/Users/jonathangallo/ANDROID_REPOS/firstapp/PROJECT_CONTEXT.txt): detailed internal project report

## Build

Build a debug APK with:

```bash
./gradlew :app:assembleDebug
```

Output:

- [`app-debug.apk`](/Users/jonathangallo/ANDROID_REPOS/firstapp/app/build/outputs/apk/debug/app-debug.apk)

## Current Limitations

- Timers are in-memory only
- Timers do not survive app restarts or process death
- Theme and accent settings are not yet persisted to durable storage
- No notifications yet
- No background alarm scheduling yet
- No automated tests yet

## Likely Next Steps

- Persist timers and settings with DataStore
- Add notifications for completed timers
- Add proper launcher icon assets
- Add optional custom sound/vibration settings
- Refactor state out of `MainActivity.kt` as the app grows
