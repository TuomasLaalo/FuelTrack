package com.laatu.fueltank.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.data.local.FuelTankDatabase
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.model.TripStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FuelTankRepositoryTest {
    private lateinit var database: FuelTankDatabase
    private lateinit var carRepository: OfflineCarRepository
    private lateinit var fuelEntryRepository: OfflineFuelEntryRepository
    private lateinit var tripRepository: OfflineTripRepository
    private lateinit var tripBreakRepository: OfflineTripBreakRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FuelTankDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        carRepository = OfflineCarRepository(database.carDao())
        fuelEntryRepository = OfflineFuelEntryRepository(database, database.fuelEntryDao())
        tripRepository = OfflineTripRepository(database.tripDao())
        tripBreakRepository = OfflineTripBreakRepository(database.tripBreakDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun carRepository_upsertsAndReadsDomainCar() = runBlocking {
        carRepository.upsertCar(testCar(name = "Corolla"))

        val car = carRepository.getPrimaryCar()

        assertThat(car?.name).isEqualTo("Corolla")
        assertThat(car?.tankSizeMilliliters).isEqualTo(55_000)
        assertThat(car?.initialOdometerKm).isEqualTo(10_000)
    }

    @Test
    fun fuelEntryRepository_applyMutationDeletesAndUpsertsInOneCall() = runBlocking {
        carRepository.upsertCar(testCar())
        fuelEntryRepository.createFuelEntry(testFuelEntry(id = 1, odometerKm = 10_100))
        fuelEntryRepository.createFuelEntry(testFuelEntry(id = 2, odometerKm = 10_250))

        fuelEntryRepository.applyMutation(
            upsertEntries = listOf(
                testFuelEntry(id = 1, odometerKm = 10_150).copy(
                    stationBrand = "Neste",
                    note = "corrected",
                ),
            ),
            deleteEntryIds = listOf(2),
        )

        val entries = fuelEntryRepository.getEntriesForCar(1)
        assertThat(entries.map { it.id }).containsExactly(1L)
        assertThat(entries.single().odometerKm).isEqualTo(10_150)
        assertThat(entries.single().stationBrand).isEqualTo("Neste")
        assertThat(entries.single().note).isEqualTo("corrected")
    }

    @Test
    fun tripRepository_upsertsAndReturnsNewestActiveTrip() = runBlocking {
        carRepository.upsertCar(testCar())
        val firstTripId = tripRepository.upsertTrip(testTrip(status = TripStatus.ACTIVE, startTimeEpochMillis = 100))
        val completedTripId = tripRepository.upsertTrip(testTrip(status = TripStatus.COMPLETED, startTimeEpochMillis = 300))
        val newestActiveTripId = tripRepository.upsertTrip(testTrip(status = TripStatus.ACTIVE, startTimeEpochMillis = 200))

        val activeTrip = tripRepository.getActiveTripForCar(1)
        val trips = tripRepository.getTripsForCar(1)

        assertThat(activeTrip?.id).isEqualTo(newestActiveTripId)
        assertThat(trips.map { it.id }).containsExactly(completedTripId, newestActiveTripId, firstTripId).inOrder()
    }

    @Test
    fun tripBreakRepository_ordersUpdatesAndDeletesBreaks() = runBlocking {
        carRepository.upsertCar(testCar())
        val tripId = tripRepository.upsertTrip(testTrip(status = TripStatus.ACTIVE))
        val activeBreakId = tripBreakRepository.upsertBreak(
            testTripBreak(tripId = tripId, startTimeEpochMillis = 300, endTimeEpochMillis = null),
        )
        val completedBreakId = tripBreakRepository.upsertBreak(
            testTripBreak(tripId = tripId, startTimeEpochMillis = 100, endTimeEpochMillis = 150),
        )

        val activeBreak = tripBreakRepository.getActiveBreakForTrip(tripId)
        assertThat(activeBreak?.id).isEqualTo(activeBreakId)

        tripBreakRepository.upsertBreak(
            testTripBreak(id = activeBreakId, tripId = tripId, startTimeEpochMillis = 300, endTimeEpochMillis = 350),
        )
        tripBreakRepository.deleteBreak(completedBreakId)

        val breaks = tripBreakRepository.getBreaksForTrip(tripId)
        assertThat(breaks.map { it.id }).containsExactly(activeBreakId)
        assertThat(breaks.single().endTimeEpochMillis).isEqualTo(350)
    }

    private fun testCar(
        name: String = "Test car",
    ) = Car(
        id = 1,
        name = name,
        tankSizeMilliliters = 55_000,
        initialOdometerKm = 10_000,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 1,
    )

    private fun testFuelEntry(
        id: Long,
        odometerKm: Long,
    ) = FuelEntry(
        id = id,
        carId = 1,
        tripId = null,
        dateTimeEpochMillis = id,
        distanceInputMode = DistanceInputMode.ODOMETER,
        odometerKm = odometerKm,
        tripMeterKm = null,
        litersMilliliters = 40_000,
        totalCostCents = 6_000,
        pricePerLiterMicros = 1_500_000,
        stationBrand = null,
        isFullTank = true,
        note = null,
        createdAtEpochMillis = id,
        updatedAtEpochMillis = id,
    )

    private fun testTrip(
        id: Long = 0,
        status: TripStatus,
        startTimeEpochMillis: Long = id,
    ) = Trip(
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
        id: Long = 0,
        tripId: Long,
        startTimeEpochMillis: Long = id,
        endTimeEpochMillis: Long? = startTimeEpochMillis + 100,
    ) = TripBreak(
        id = id,
        tripId = tripId,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = endTimeEpochMillis,
        note = null,
        createdAtEpochMillis = startTimeEpochMillis,
        updatedAtEpochMillis = endTimeEpochMillis ?: startTimeEpochMillis,
    )
}
