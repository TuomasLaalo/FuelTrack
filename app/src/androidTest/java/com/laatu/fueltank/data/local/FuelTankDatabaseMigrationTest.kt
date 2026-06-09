package com.laatu.fueltank.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FuelTankDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = FuelTankDatabase::class.java,
    )

    @Test
    fun migrate1To2_addsStationBrandAndPreservesFuelEntryData() {
        helper.createDatabase(TEST_DB, 1).apply {
            insertCar()
            insertTrip()
            execSQL(
                """
                INSERT INTO fuel_entries (
                    id,
                    carId,
                    tripId,
                    dateTimeEpochMillis,
                    distanceInputMode,
                    odometerKm,
                    tripMeterKm,
                    litersMilliliters,
                    totalCostCents,
                    pricePerLiterMicros,
                    isFullTank,
                    note,
                    createdAtEpochMillis,
                    updatedAtEpochMillis
                ) VALUES (
                    11,
                    1,
                    7,
                    1000,
                    'ODOMETER',
                    120500,
                    NULL,
                    42000,
                    7800,
                    1857142,
                    1,
                    'before migration',
                    1000,
                    1000
                )
                """.trimIndent(),
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            FuelTankDatabase.MIGRATION_1_2,
        )

        val fuelEntryCursor = migrated.query("SELECT stationBrand, note FROM fuel_entries WHERE id = 11")
        try {
            val cursor = fuelEntryCursor
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.isNull(0)).isTrue()
            assertThat(cursor.getString(1)).isEqualTo("before migration")
        } finally {
            fuelEntryCursor.close()
        }
    }

    @Test
    fun migrate2To3_addsTripBreaksTableAndPreservesTripData() {
        helper.createDatabase(TEST_DB, 2).apply {
            insertCar()
            insertTrip()
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            FuelTankDatabase.MIGRATION_2_3,
        )

        val tripCursor = migrated.query("SELECT title FROM trips WHERE id = 7")
        try {
            val cursor = tripCursor
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Migration trip")
        } finally {
            tripCursor.close()
        }

        migrated.execSQL(
            """
            INSERT INTO trip_breaks (
                id,
                tripId,
                startTimeEpochMillis,
                endTimeEpochMillis,
                note,
                createdAtEpochMillis,
                updatedAtEpochMillis
            ) VALUES (
                21,
                7,
                1200,
                1500,
                'coffee',
                1200,
                1500
            )
            """.trimIndent(),
        )
        val breakCursor = migrated.query("SELECT note FROM trip_breaks WHERE id = 21")
        try {
            val cursor = breakCursor
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("coffee")
        } finally {
            breakCursor.close()
        }
    }

    private fun SupportSQLiteDatabase.insertCar() {
        execSQL(
            """
            INSERT INTO cars (
                id,
                name,
                tankSizeMilliliters,
                initialOdometerKm,
                createdAtEpochMillis,
                updatedAtEpochMillis
            ) VALUES (
                1,
                'Migration car',
                55000,
                120000,
                100,
                100
            )
            """.trimIndent(),
        )
    }

    private fun SupportSQLiteDatabase.insertTrip() {
        execSQL(
            """
            INSERT INTO trips (
                id,
                carId,
                title,
                startLocation,
                endLocation,
                startOdometerKm,
                endOdometerKm,
                startTimeEpochMillis,
                endTimeEpochMillis,
                status,
                createdAtEpochMillis,
                updatedAtEpochMillis
            ) VALUES (
                7,
                1,
                'Migration trip',
                'A',
                'B',
                120000,
                120500,
                1000,
                2000,
                'COMPLETED',
                1000,
                2000
            )
            """.trimIndent(),
        )
    }

    private companion object {
        const val TEST_DB = "fuel-tank-migration-test"
    }
}
