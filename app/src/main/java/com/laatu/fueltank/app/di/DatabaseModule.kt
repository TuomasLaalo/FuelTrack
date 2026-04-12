package com.laatu.fueltank.app.di

import android.content.Context
import androidx.room.Room
import com.laatu.fueltank.data.local.FuelTankDatabase
import com.laatu.fueltank.data.local.dao.CarDao
import com.laatu.fueltank.data.local.dao.FuelEntryDao
import com.laatu.fueltank.data.local.dao.TripDao
import com.laatu.fueltank.data.repository.OfflineCarRepository
import com.laatu.fueltank.data.repository.OfflineFuelEntryRepository
import com.laatu.fueltank.data.repository.OfflineTripRepository
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FuelTankDatabase =
        Room.databaseBuilder(
            context,
            FuelTankDatabase::class.java,
            "fuel_tank.db",
        )
            .addMigrations(FuelTankDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideCarDao(database: FuelTankDatabase): CarDao = database.carDao()

    @Provides
    fun provideFuelEntryDao(database: FuelTankDatabase): FuelEntryDao = database.fuelEntryDao()

    @Provides
    fun provideTripDao(database: FuelTankDatabase): TripDao = database.tripDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCarRepository(repository: OfflineCarRepository): CarRepository

    @Binds
    abstract fun bindFuelEntryRepository(repository: OfflineFuelEntryRepository): FuelEntryRepository

    @Binds
    abstract fun bindTripRepository(repository: OfflineTripRepository): TripRepository
}
