package com.laatu.fueltank.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TripUseCasesTest {
    @Test
    fun startTrip_usesNewestKnownOdometerFromTripHistory() = runTest {
        val carRepository = FakeTripCarRepository(testCar())
        val fuelEntryRepository = FakeTripFuelEntryRepository(
            mutableListOf(
                testFuelEntry(id = 1, odometerKm = 10_300),
            ),
        )
        val tripRepository = FakeTripRepository(
            trips = mutableListOf(
                testTrip(id = 3, status = TripStatus.COMPLETED, startOdometerKm = 10_200, endOdometerKm = 10_450),
            ),
        )
        val useCase = StartTripUseCase(
            carRepository = carRepository,
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = tripRepository,
            currentOdometerService = CurrentOdometerService(),
        )

        val tripId = useCase(
            title = "Work route",
            startLocation = "Home",
        )

        assertThat(tripId).isEqualTo(4L)
        val startedTrip = tripRepository.trips.first { it.id == tripId }
        assertThat(startedTrip.startOdometerKm).isEqualTo(10_450)
        assertThat(startedTrip.status).isEqualTo(TripStatus.ACTIVE)
    }

    @Test
    fun endActiveTrip_tripMeterModeUsesStartOdometerBaseline() = runTest {
        val carRepository = FakeTripCarRepository(testCar())
        val tripRepository = FakeTripRepository(
            trips = mutableListOf(
                testTrip(id = 9, status = TripStatus.ACTIVE, startOdometerKm = 10_500, endOdometerKm = null),
            ),
        )
        val useCase = EndActiveTripUseCase(
            carRepository = carRepository,
            tripRepository = tripRepository,
        )

        val result = useCase(
            distanceInputMode = DistanceInputMode.TRIP_METER,
            odometerKm = null,
            tripMeterKm = 180,
            endLocation = "Cabin",
        )

        assertThat(result).isEqualTo(9L)
        val endedTrip = tripRepository.trips.single()
        assertThat(endedTrip.endOdometerKm).isEqualTo(10_680)
        assertThat(endedTrip.status).isEqualTo(TripStatus.COMPLETED)
        assertThat(endedTrip.endLocation).isEqualTo("Cabin")
    }
}

private class FakeTripCarRepository(
    primaryCar: Car?,
) : CarRepository {
    private val state = MutableStateFlow(primaryCar)

    override fun observePrimaryCar(): Flow<Car?> = state.asStateFlow()

    override suspend fun getPrimaryCar(): Car? = state.value

    override suspend fun upsertCar(car: Car): Long {
        state.value = car
        return car.id
    }
}

private class FakeTripFuelEntryRepository(
    private val entries: MutableList<FuelEntry>,
) : FuelEntryRepository {
    private val state = MutableStateFlow(entries.toList())

    override fun observeEntriesForCar(carId: Long): Flow<List<FuelEntry>> = state.asStateFlow()

    override suspend fun getEntriesForCar(carId: Long): List<FuelEntry> = entries.filter { it.carId == carId }

    override suspend fun createFuelEntry(fuelEntry: FuelEntry): Long {
        entries += fuelEntry
        state.value = entries.toList()
        return fuelEntry.id
    }

    override suspend fun getFuelEntry(entryId: Long): FuelEntry? = entries.firstOrNull { it.id == entryId }

    override suspend fun updateFuelEntry(fuelEntry: FuelEntry) {
        val index = entries.indexOfFirst { it.id == fuelEntry.id }
        if (index >= 0) {
            entries[index] = fuelEntry
            state.value = entries.toList()
        }
    }

    override suspend fun deleteFuelEntry(entryId: Long) {
        entries.removeAll { it.id == entryId }
        state.value = entries.toList()
    }

    override suspend fun applyMutation(upsertEntries: List<FuelEntry>, deleteEntryIds: List<Long>) {
        deleteEntryIds.forEach { deleteFuelEntry(it) }
        upsertEntries.forEach { updateFuelEntry(it) }
    }
}

private class FakeTripRepository(
    val trips: MutableList<Trip>,
) : TripRepository {
    private val state = MutableStateFlow(trips.toList())

    override fun observeTripsForCar(carId: Long): Flow<List<Trip>> = state.asStateFlow()

    override fun observeActiveTripForCar(carId: Long): Flow<Trip?> =
        MutableStateFlow(trips.firstOrNull { it.carId == carId && it.status == TripStatus.ACTIVE }).asStateFlow()

    override fun observeTrip(tripId: Long): Flow<Trip?> =
        MutableStateFlow(trips.firstOrNull { it.id == tripId }).asStateFlow()

    override suspend fun getTripsForCar(carId: Long): List<Trip> = trips.filter { it.carId == carId }

    override suspend fun getActiveTripForCar(carId: Long): Trip? =
        trips.firstOrNull { it.carId == carId && it.status == TripStatus.ACTIVE }

    override suspend fun getTrip(tripId: Long): Trip? = trips.firstOrNull { it.id == tripId }

    override suspend fun upsertTrip(trip: Trip): Long {
        return if (trip.id == 0L) {
            val assignedId = (trips.maxOfOrNull { it.id } ?: 0L) + 1L
            trips += trip.copy(id = assignedId)
            state.value = trips.toList()
            assignedId
        } else {
            val index = trips.indexOfFirst { it.id == trip.id }
            if (index >= 0) {
                trips[index] = trip
            } else {
                trips += trip
            }
            state.value = trips.toList()
            trip.id
        }
    }

    override suspend fun deleteTrip(tripId: Long) {
        trips.removeAll { it.id == tripId }
        state.value = trips.toList()
    }
}

private fun testCar() = Car(
    id = 1,
    name = "Test car",
    tankSizeMilliliters = 60_000,
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
    isFullTank = true,
    note = null,
    createdAtEpochMillis = id,
    updatedAtEpochMillis = id,
)

private fun testTrip(
    id: Long,
    status: TripStatus,
    startOdometerKm: Long,
    endOdometerKm: Long?,
) = Trip(
    id = id,
    carId = 1,
    title = "Trip",
    startLocation = "Home",
    endLocation = null,
    startOdometerKm = startOdometerKm,
    endOdometerKm = endOdometerKm,
    startTimeEpochMillis = id,
    endTimeEpochMillis = if (status == TripStatus.COMPLETED) id + 1 else null,
    status = status,
    createdAtEpochMillis = id,
    updatedAtEpochMillis = id,
)
