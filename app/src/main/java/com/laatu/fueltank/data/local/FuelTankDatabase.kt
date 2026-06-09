package com.laatu.fueltank.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.laatu.fueltank.data.local.converter.RoomConverters
import com.laatu.fueltank.data.local.dao.CarDao
import com.laatu.fueltank.data.local.dao.FuelEntryDao
import com.laatu.fueltank.data.local.dao.TripBreakDao
import com.laatu.fueltank.data.local.dao.TripDao
import com.laatu.fueltank.data.local.entity.CarEntity
import com.laatu.fueltank.data.local.entity.FuelEntryEntity
import com.laatu.fueltank.data.local.entity.TripBreakEntity
import com.laatu.fueltank.data.local.entity.TripEntity

@Database(
    entities = [CarEntity::class, FuelEntryEntity::class, TripEntity::class, TripBreakEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class FuelTankDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun tripDao(): TripDao
    abstract fun tripBreakDao(): TripBreakDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS trip_breaks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        startTimeEpochMillis INTEGER NOT NULL,
                        endTimeEpochMillis INTEGER,
                        note TEXT,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(tripId) REFERENCES trips(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_trip_breaks_tripId_startTimeEpochMillis
                    ON trip_breaks(tripId, startTimeEpochMillis)
                    """.trimIndent(),
                )
            }
        }
    }
}
