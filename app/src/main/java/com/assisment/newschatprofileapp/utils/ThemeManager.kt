package com.assisment.newschatprofileapp.utils



import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
        private const val DEFAULT_THEME = "system"

    }

    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            val value = preferences[THEME_PREFERENCE_KEY] ?: DEFAULT_THEME
            when (value) {
                "light" -> ThemePreference.LIGHT
                "dark" -> ThemePreference.DARK
                else -> ThemePreference.SYSTEM
            }
        }


    suspend fun setThemePreference(preference: ThemePreference) {
        context.dataStore.edit { settings ->
            val value = when (preference) {
                ThemePreference.LIGHT -> "light"
                ThemePreference.DARK -> "dark"
                ThemePreference.SYSTEM -> "system"
            }
            settings[THEME_PREFERENCE_KEY] = value
        }
    }
}