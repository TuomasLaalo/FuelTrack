package com.laatu.fueltank

import android.app.Application
import com.laatu.fueltank.core.settings.AppLanguageManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FuelTankApp : Application() {
    @Inject
    lateinit var appLanguageManager: AppLanguageManager

    override fun onCreate() {
        super.onCreate()
        appLanguageManager.applySavedLanguage()
    }
}
