package com.example.firstapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.firstapp.ui.theme.AppAccentColor
import com.example.firstapp.ui.theme.AppThemeMode
import com.example.firstapp.ui.theme.ControllerHighlightColor
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val accentColor: AppAccentColor = AppAccentColor.RED,
    val controllerHighlightColor: ControllerHighlightColor = ControllerHighlightColor.DEFAULT
)

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

class AppSettingsStore(private val context: Context) {
    val settings: Flow<AppSettings> = context.appSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[ThemeModeKey].toThemeMode(),
                accentColor = preferences[AccentColorKey].toAccentColor(),
                controllerHighlightColor = preferences[ControllerHighlightColorKey].toControllerHighlightColor()
            )
        }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[ThemeModeKey] = themeMode.name
        }
    }

    suspend fun setAccentColor(accentColor: AppAccentColor) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[AccentColorKey] = accentColor.name
        }
    }

    suspend fun setControllerHighlightColor(controllerHighlightColor: ControllerHighlightColor) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[ControllerHighlightColorKey] = controllerHighlightColor.name
        }
    }

    private fun String?.toThemeMode(): AppThemeMode {
        return runCatching { AppThemeMode.valueOf(this.orEmpty()) }
            .getOrDefault(AppThemeMode.DARK)
    }

    private fun String?.toAccentColor(): AppAccentColor {
        return runCatching { AppAccentColor.valueOf(this.orEmpty()) }
            .getOrDefault(AppAccentColor.RED)
    }

    private fun String?.toControllerHighlightColor(): ControllerHighlightColor {
        return runCatching { ControllerHighlightColor.valueOf(this.orEmpty()) }
            .getOrDefault(ControllerHighlightColor.DEFAULT)
    }

    private companion object {
        val ThemeModeKey = stringPreferencesKey("theme_mode")
        val AccentColorKey = stringPreferencesKey("accent_color")
        val ControllerHighlightColorKey = stringPreferencesKey("controller_highlight_color")
    }
}
