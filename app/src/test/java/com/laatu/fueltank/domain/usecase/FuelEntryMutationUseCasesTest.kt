package com.laatu.fueltank.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.service.FuelEntryMutationPlanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FuelEntryMutationUseCasesTest {
    @Test
    fun updateFuelEntryUseCase_appliesRecalculatedChain() = runTest {
        val carRepository = FakeCarRepository(primaryCar = testCar())
        val fuelEntryRepository = FakeFuelEntryRepository(
            entries = mutableListOf(
                entry(id = 1, dateTimeEpochMillis = 1, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_100),
                entry(id = 2, dateTimeEpochMillis = 2, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_250, tripMeterKm = 150),
                entry(id = 3, dateTimeEpochMillis = 3, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_350, tripMeterKm = 100),
            ),
        )
        val useCase = UpdateFuelEntryUseCase(
            carRepository = carRepository,
            fuelEntryRepository = fuelEntryRepository,
            fuelEntryMutationPlanner = FuelEntryMutationPlanner(),
        )

        useCase(
            entryId = 1,
            carId = 1,
            distanceInputMode = DistanceInputMode.ODOMETER,
            odometerKm = 10_220,
            tripMeterKm = null,
            litersMilliliters = 40_000,
            totalCostCents = 6_000,
            pricePerLiterMicros = 1_500_000,
            stationBrand = "Shell",
            isFullTank = true,
            note = "updated",
        )

        assertThat(fuelEntryRepository.lastAppliedDeleteIds).isEmpty()
        assertThat(fuelEntryRepository.lastAppliedEntries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_220L,
            2L to 10_370L,
            3L to 10_470L,
        ).inOrder()
        assertThat(fuelEntryRepository.entries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_220L,
            2L to 10_370L,
            3L to 10_470L,
        ).inOrder()
    }

    @Test
    fun deleteFuelEntryUseCase_deletesTargetAndRecalculatesFollowingEntries() = runTest {
        val carRepository = FakeCarRepository(primaryCar = testCar())
        val fuelEntryRepository = FakeFuelEntryRepository(
            entries = mutableListOf(
                entry(id = 1, dateTimeEpochMillis = 1, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_100),
                entry(id = 2, dateTimeEpochMillis = 2, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_250, tripMeterKm = 150),
                entry(id = 3, dateTimeEpochMillis = 3, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_350, tripMeterKm = 100),
            ),
        )
        val useCase = DeleteFuelEntryUseCase(
            carRepository = carRepository,
            fuelEntryRepository = fuelEntryRepository,
            fuelEntryMutationPlanner = FuelEntryMutationPlanner(),
        )

        useCase(entryId = 2)

        assertThat(fuelEntryRepository.lastAppliedDeleteIds).containsExactly(2L)
        assertThat(fuelEntryRepository.lastAppliedEntries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_100L,
            3L to 10_200L,
        ).inOrder()
        assertThat(fuelEntryRepository.entries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_100L,
            3L to 10_200L,
        ).inOrder()
    }
}

private class FakeCarRepository(
    primaryCar: Car?,
) : CarRepository {
    private val carState = MutableStateFlow(primaryCar)

    override fun observePrimaryCar(): Flow<Car?> = carState.asStateFlow()

    override suspend fun getPrimaryCar(): Car? = carState.value

    override suspend fun upsertCar(car: Car): Long {
        carState.value = car
        return car.id
    }
}

private class FakeFuelEntryRepository(
    val entries: MutableList<FuelEntry>,
) : FuelEntryRepository {
    private val entriesFlow = MutableStateFlow(entries.sortedByDescending { it.dateTimeEpochMillis })

    var lastAppliedEntries: List<FuelEntry> = emptyList()
        private set
    var lastAppliedDeleteIds: List<Long> = emptyList()
        private set

    override fun observeEntriesForCar(carId: Long): Flow<List<FuelEntry>> = entriesFlow.asStateFlow()

    override suspend fun getEntriesForCar(carId: Long): List<FuelEntry> =
        entries
            .filter { it.carId == carId }
            .sortedWith(compareBy<FuelEntry> { it.dateTimeEpochMillis }.thenBy { it.createdAtEpochMillis }.thenBy { it.id })

    override suspend fun createFuelEntry(fuelEntry: FuelEntry): Long {
        entries += fuelEntry
        sync()
        return fuelEntry.id
    }

    override suspend fun getFuelEntry(entryId: Long): FuelEntry? = entries.firstOrNull { it.id == entryId }

    override suspend fun updateFuelEntry(fuelEntry: FuelEntry) {
        val index = entries.indexOfFirst { it.id == fuelEntry.id }
        if (index >= 0) {
            entries[index] = fuelEntry
            sync()
        }
    }

    override suspend fun deleteFuelEntry(entryId: Long) {
        entries.removeAll { it.id == entryId }
        sync()
    }

    override suspend fun applyMutation(upsertEntries: List<FuelEntry>, deleteEntryIds: List<Long>) {
        lastAppliedEntries = upsertEntries
        lastAppliedDeleteIds = deleteEntryIds

        if (deleteEntryIds.isNotEmpty()) {
            entries.removeAll { it.id in deleteEntryIds }
        }
        upsertEntries.forEach { updated ->
            val index = entries.indexOfFirst { it.id == updated.id }
            if (index >= 0) {
                entries[index] = updated
            } else {
                entries += updated
            }
        }
        sync()
    }

    private fun sync() {
        entriesFlow.value = entries.sortedByDescending { it.dateTimeEpochMillis }
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

private fun entry(
    id: Long,
    dateTimeEpochMillis: Long,
    distanceInputMode: DistanceInputMode,
    odometerKm: Long,
    tripMeterKm: Long? = null,
) = FuelEntry(
    id = id,
    carId = 1,
    tripId = null,
    dateTimeEpochMillis = dateTimeEpochMillis,
    distanceInputMode = distanceInputMode,
    odometerKm = odometerKm,
    tripMeterKm = tripMeterKm,
    litersMilliliters = 40_000,
    totalCostCents = 6_000,
    pricePerLiterMicros = 1_500_000,
    stationBrand = null,
    isFullTank = true,
    note = null,
    createdAtEpochMillis = dateTimeEpochMillis,
    updatedAtEpochMillis = dateTimeEpochMillis,
)
