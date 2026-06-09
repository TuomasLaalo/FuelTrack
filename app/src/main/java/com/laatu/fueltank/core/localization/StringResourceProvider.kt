package com.laatu.fueltank.core.localization

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidStringResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : StringResourceProvider {
    override fun get(@StringRes resId: Int, vararg args: Any): String =
        localizedContext().getString(resId, *args)

    override fun currentLocale(): Locale =
        selectedLocale() ?: context.resources.configuration.locales[0] ?: Locale.getDefault()

    private fun localizedContext(): Context {
        val locale = selectedLocale() ?: return context
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun selectedLocale(): Locale? =
        AppCompatDelegate.getApplicationLocales().takeUnless { it.isEmpty }?.get(0)
}

interface StringResourceProvider {
    fun get(@StringRes resId: Int, vararg args: Any): String

    fun currentLocale(): Locale
}
