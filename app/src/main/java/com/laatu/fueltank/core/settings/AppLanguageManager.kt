package com.laatu.fueltank.core.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AppLanguageManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val currentLanguageState = MutableStateFlow(loadLanguage())

    val currentLanguage: StateFlow<AppLanguage> = currentLanguageState.asStateFlow()

    fun applySavedLanguage() {
        applyLanguage(loadLanguage())
    }

    fun setLanguage(language: AppLanguage) {
        preferences.edit().putString(KEY_APP_LANGUAGE, language.storageValue).apply()
        applyLanguage(language)
    }

    private fun loadLanguage(): AppLanguage =
        AppLanguage.fromStorageValue(preferences.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.storageValue))

    private fun applyLanguage(language: AppLanguage) {
        // Locale override is applied at the app level so Compose screens and ViewModel-owned
        // string formatting both observe the same language selection after startup and changes.
        val locales = language.languageTag
            ?.let { LocaleListCompat.forLanguageTags(it) }
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
        currentLanguageState.value = language
    }

    private companion object {
        const val PREFERENCES_NAME = "fueltrack_settings"
        const val KEY_APP_LANGUAGE = "app_language"
    }
}
