package com.laatu.fueltank.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.data.local.entity.CarEntity
import com.laatu.fueltank.data.local.entity.FuelEntryEntity
import com.laatu.fueltank.data.local.entity.TripBreakEntity
import com.laatu.fueltank.data.local.entity.TripEntity
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.TripStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FuelTankDaoTest {
    private lateinit var database: FuelTankDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FuelTankDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun carDao_upsertsAndReadsPrimaryCar() = runBlocking {
        database.carDao().upsertCar(testCar(id = 1, name = "Corolla"))
        database.carDao().upsertCar(testCar(id = 2, name = "Golf"))

        val primaryCar = database.carDao().getPrimaryCar()

        assertThat(primaryCar?.id).isEqualTo(1)
        assertThat(primaryCar?.name).isEqualTo("Corolla")
    }

    @Test
    fun fuelEntryDao_readsEntriesChronologicallyAndSupportsUpdateAndDelete() = runBlocking {
        database.carDao().upsertCar(testCar())
        database.fuelEntryDao().insertFuelEntry(testFuelEntry(id = 3, dateTimeEpochMillis = 300, createdAtEpochMillis = 1))
        database.fuelEntryDao().insertFuelEntry(testFuelEntry(id = 1, dateTimeEpochMillis = 100, createdAtEpochMillis = 1))
        database.fuelEntryDao().insertFuelEntry(testFuelEntry(id = 2, dateTimeEpochMillis = 100, createdAtEpochMillis = 2))

        assertThat(database.fuelEntryDao().getEntriesForCar(1).map { it.id }).containsExactly(1L, 2L, 3L).inOrder()

        database.fuelEntryDao().updateFuelEntry(
            testFuelEntry(id = 2, dateTimeEpochMillis = 100, createdAtEpochMillis = 2).copy(
                odometerKm = 10_250,
                stationBrand = "Neste",
            ),
        )

        val updatedEntry = database.fuelEntryDao().getFuelEntry(2)
        assertThat(updatedEntry?.odometerKm).isEqualTo(10_250)
        assertThat(updatedEntry?.stationBrand).isEqualTo("Neste")

        database.fuelEntryDao().deleteFuelEntry(1)

        assertThat(database.fuelEntryDao().getEntriesForCar(1).map { it.id }).containsExactly(2L, 3L).inOrder()
    }

    @Test
    fun tripDao_returnsNewestActiveTripForCar() = runBlocking {
        database.carDao().upsertCar(testCar())
        database.tripDao().insertTrip(testTrip(id = 1, status = TripStatus.ACTIVE, startTimeEpochMillis = 100))
        database.tripDao().insertTrip(testTrip(id = 2, status = TripStatus.COMPLETED, startTimeEpochMillis = 300))
        database.tripDao().insertTrip(testTrip(id = 3, status = TripStatus.ACTIVE, startTimeEpochMillis = 200))

        val activeTrip = database.tripDao().getActiveTripForCar(1)
        val trips = database.tripDao().getTripsForCar(1)

        assertThat(activeTrip?.id).isEqualTo(3)
        assertThat(trips.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
    }

    @Test
    fun tripBreakDao_ordersBreaksAndReturnsLatestActiveBreak() = runBlocking {
        database.carDao().upsertCar(testCar())
        database.tripDao().insertTrip(testTrip(id = 7, status = TripStatus.ACTIVE))
        database.tripBreakDao().insertBreak(testTripBreak(id = 1, tripId = 7, startTimeEpochMillis = 300, endTimeEpochMillis = null))
        database.tripBreakDao().insertBreak(testTripBreak(id = 2, tripId = 7, startTimeEpochMillis = 100, endTimeEpochMillis = 150))
        database.tripBreakDao().insertBreak(testTripBreak(id = 3, tripId = 7, startTimeEpochMillis = 400, endTimeEpochMillis = null))

        val breaks = database.tripBreakDao().getBreaksForTrip(7)
        val activeBreak = database.tripBreakDao().getActiveBreakForTrip(7)

        assertThat(breaks.map { it.id }).containsExactly(2L, 1L, 3L).inOrder()
        assertThat(activeBreak?.id).isEqualTo(3)
    }

    @Test
    fun deletingTripClearsLinkedFuelEntriesAndDeletesTripBreaks() = runBlocking {
        database.carDao().upsertCar(testCar())
        database.tripDao().insertTrip(testTrip(id = 7, status = TripStatus.COMPLETED))
        database.fuelEntryDao().insertFuelEntry(testFuelEntry(id = 11, tripId = 7))
        database.tripBreakDao().insertBreak(testTripBreak(id = 21, tripId = 7))

        database.tripDao().deleteTripById(7)

        assertThat(database.fuelEntryDao().getFuelEntry(11)?.tripId).isNull()
        assertThat(database.tripBreakDao().getBreaksForTrip(7)).isEmpty()
    }

    private fun testCar(
        id: Long = 1,
        name: String = "Test car",
    ) = CarEntity(
        id = id,
        name = name,
        tankSizeMilliliters = 55_000,
        initialOdometerKm = 10_000,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 1,
    )

    private fun testFuelEntry(
        id: Long,
        carId: Long = 1,
        tripId: Long? = null,
        dateTimeEpochMillis: Long = id,
        createdAtEpochMillis: Long = id,
    ) = FuelEntryEntity(
        id = id,
        carId = carId,
        tripId = tripId,
        dateTimeEpochMillis = dateTimeEpochMillis,
        distanceInputMode = DistanceInputMode.ODOMETER,
        odometerKm = 10_000 + id,
        tripMeterKm = null,
        litersMilliliters = 40_000,
        totalCostCents = 6_000,
        pricePerLiterMicros = 1_500_000,
        stationBrand = null,
        isFullTank = true,
        note = null,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = createdAtEpochMillis,
    )

    private fun testTrip(
        id: Long,
        status: TripStatus,
        startTimeEpochMillis: Long = id,
    ) = TripEntity(
        id = id,
        carId = 1,
        title = "Trip $id",
        startLocation = "A",
        endLocation = if (status == TripStatus.COMPLETED) "B" else null,
        startOdometerKm = 10_000,
        endOdometerKm = if (status == TripStatus.COMPLETED) 10_500 else null,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = if (status == TripStatus.COMPLETED) startTimeEpochMillis + 1_000 else null,
        status = status,
        createdAtEpochMillis = startTimeEpochMillis,
        updatedAtEpochMillis = startTimeEpochMillis,
    )

    private fun testTripBreak(
        id: Long,
        tripId: Long,
        startTimeEpochMillis: Long = id,
        endTimeEpochMillis: Long? = startTimeEpochMillis + 100,
    ) = TripBreakEntity(
        id = id,
        tripId = tripId,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = endTimeEpochMillis,
        note = null,
        createdAtEpochMillis = startTimeEpochMillis,
        updatedAtEpochMillis = endTimeEpochMillis ?: startTimeEpochMillis,
    )
}
