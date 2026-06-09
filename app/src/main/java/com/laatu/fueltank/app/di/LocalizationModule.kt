package com.laatu.fueltank.app.di

import com.laatu.fueltank.core.localization.AndroidStringResourceProvider
import com.laatu.fueltank.core.localization.StringResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalizationModule {
    @Binds
    abstract fun bindStringResourceProvider(
        provider: AndroidStringResourceProvider,
    ): StringResourceProvider
}
