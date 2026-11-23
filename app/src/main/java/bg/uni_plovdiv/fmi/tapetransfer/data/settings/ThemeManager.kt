package bg.uni_plovdiv.fmi.tapetransfer.data.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode(val nightMode: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
}

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        val saved = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return ThemeMode.valueOf(saved)
    }

    fun setThemeMode(mode: ThemeMode) {
        if (mode == getThemeMode()) return
        prefs.edit { putString(KEY_THEME, mode.name) }
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
    }

    fun applySavedTheme() {
        AppCompatDelegate.setDefaultNightMode(getThemeMode().nightMode)
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
    }
}
