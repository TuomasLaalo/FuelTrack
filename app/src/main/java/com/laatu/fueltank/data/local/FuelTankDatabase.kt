package com.laatu.fueltank.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.laatu.fueltank.data.local.converter.RoomConverters
import com.laatu.fueltank.data.local.dao.CarDao
import com.laatu.fueltank.data.local.dao.FuelEntryDao
import com.laatu.fueltank.data.local.dao.TripDao
import com.laatu.fueltank.data.local.entity.CarEntity
import com.laatu.fueltank.data.local.entity.FuelEntryEntity
import com.laatu.fueltank.data.local.entity.TripEntity

@Database(
    entities = [CarEntity::class, FuelEntryEntity::class, TripEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class FuelTankDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun tripDao(): TripDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE fuel_entries
                    ADD COLUMN stationBrand TEXT
                    """.trimIndent(),
                )
            }
        }
    }
}
