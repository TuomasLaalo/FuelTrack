package com.laatu.fueltank.feature.fuelentry

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.MainDispatcherRule
import com.laatu.fueltank.TestStringResourceProvider
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.usecase.CreateFuelEntryUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddFuelEntryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitWithoutCar_setsGeneralErrorAndDoesNotSave() = runTest {
        val fuelEntryRepository = FakeFuelEntryRepository()
        val viewModel = viewModel(
            carRepository = FakeCarRepository(primaryCar = null),
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = FakeTripRepository(),
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.form.generalError).isEqualTo("Create a car first")
        assertThat(fuelEntryRepository.savedEntries).isEmpty()
        collectJob.cancel()
    }

    @Test
    fun submitWithInvalidTripMeterForm_setsLocalizedFieldErrorsAndDoesNotSave() = runTest {
        val fuelEntryRepository = FakeFuelEntryRepository()
        val viewModel = viewModel(
            carRepository = FakeCarRepository(primaryCar = testCar(initialOdometerKm = 10_000)),
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = FakeTripRepository(),
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.carId).isEqualTo(1)

        viewModel.submit()
        advanceUntilIdle()

        val form = viewModel.uiState.value.form
        assertThat(form.tripMeterError).isEqualTo("Enter trip meter distance")
        assertThat(form.litersError).isEqualTo("Enter fuel amount")
        assertThat(form.pricePerLiterError).isEqualTo("Enter price per liter")
        assertThat(form.isSaving).isFalse()
        assertThat(fuelEntryRepository.savedEntries).isEmpty()
        collectJob.cancel()
    }

    @Test
    fun submitWithTripMeterMode_derivesOdometerFromCurrentBaselineAndAttachesActiveTrip() = runTest {
        val fuelEntryRepository = FakeFuelEntryRepository(
            entries = mutableListOf(testFuelEntry(id = 1, odometerKm = 10_100)),
        )
        val tripRepository = FakeTripRepository(
            trips = mutableListOf(testTrip(id = 7, status = TripStatus.ACTIVE, startOdometerKm = 10_150)),
        )
        val viewModel = viewModel(
            carRepository = FakeCarRepository(primaryCar = testCar(initialOdometerKm = 10_000)),
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = tripRepository,
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        val savedEvent = async { viewModel.eventFlow.first() }
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.carId).isEqualTo(1)

        viewModel.onTripMeterChange("125")
        viewModel.onLitersChange("42,5")
        viewModel.onPricePerLiterChange("1.789")
        viewModel.onStationBrandChange("  Neste  ")
        viewModel.onNoteChange("  Fill before trip  ")
        viewModel.onFullTankChange(true)
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val savedEntry = fuelEntryRepository.savedEntries.single()
        assertThat(savedEntry.distanceInputMode).isEqualTo(DistanceInputMode.TRIP_METER)
        assertThat(savedEntry.odometerKm).isEqualTo(10_275)
        assertThat(savedEntry.tripMeterKm).isEqualTo(125)
        assertThat(savedEntry.litersMilliliters).isEqualTo(42_500)
        assertThat(savedEntry.totalCostCents).isEqualTo(7_603)
        assertThat(savedEntry.pricePerLiterMicros).isEqualTo(1_789_000)
        assertThat(savedEntry.stationBrand).isEqualTo("Neste")
        assertThat(savedEntry.note).isEqualTo("Fill before trip")
        assertThat(savedEntry.isFullTank).isTrue()
        assertThat(savedEntry.tripId).isEqualTo(7)
        assertThat(savedEvent.await()).isEqualTo(AddFuelEntryEvent.EntrySaved)
        collectJob.cancel()
    }

    @Test
    fun switchingToOdometerMode_savesDirectOdometerWithoutTripMeterValue() = runTest {
        val fuelEntryRepository = FakeFuelEntryRepository()
        val viewModel = viewModel(
            carRepository = FakeCarRepository(primaryCar = testCar(initialOdometerKm = 10_000)),
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = FakeTripRepository(),
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onDistanceModeChange(DistanceInputMode.ODOMETER)
        viewModel.onOdometerChange("10500")
        viewModel.onLitersChange("40")
        viewModel.onPricePerLiterChange("1.5")
        viewModel.onAttachToActiveTripChange(false)
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val savedEntry = fuelEntryRepository.savedEntries.single()
        assertThat(savedEntry.distanceInputMode).isEqualTo(DistanceInputMode.ODOMETER)
        assertThat(savedEntry.odometerKm).isEqualTo(10_500)
        assertThat(savedEntry.tripMeterKm).isNull()
        assertThat(savedEntry.litersMilliliters).isEqualTo(40_000)
        assertThat(savedEntry.totalCostCents).isEqualTo(6_000)
        assertThat(savedEntry.tripId).isNull()
        collectJob.cancel()
    }

    private fun viewModel(
        carRepository: FakeCarRepository,
        fuelEntryRepository: FakeFuelEntryRepository,
        tripRepository: FakeTripRepository,
    ): AddFuelEntryViewModel =
        AddFuelEntryViewModel(
            observePrimaryCarUseCase = ObservePrimaryCarUseCase(carRepository),
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = tripRepository,
            currentOdometerService = CurrentOdometerService(),
            createFuelEntryUseCase = CreateFuelEntryUseCase(fuelEntryRepository, tripRepository),
            stringResourceProvider = TestStringResourceProvider(),
        )
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
    private val entries: MutableList<FuelEntry> = mutableListOf(),
) : FuelEntryRepository {
    private val entriesState = MutableStateFlow(entries.toList())
    val savedEntries = mutableListOf<FuelEntry>()

    override fun observeEntriesForCar(carId: Long): Flow<List<FuelEntry>> = entriesState.asStateFlow()

    override suspend fun getEntriesForCar(carId: Long): List<FuelEntry> = entries.filter { it.carId == carId }

    override suspend fun createFuelEntry(fuelEntry: FuelEntry): Long {
        val assignedEntry = fuelEntry.copy(id = (entries.maxOfOrNull { it.id } ?: 0L) + 1L)
        entries += assignedEntry
        savedEntries += assignedEntry
        entriesState.value = entries.toList()
        return assignedEntry.id
    }

    override suspend fun getFuelEntry(entryId: Long): FuelEntry? = entries.firstOrNull { it.id == entryId }

    override suspend fun updateFuelEntry(fuelEntry: FuelEntry) {
        val index = entries.indexOfFirst { it.id == fuelEntry.id }
        if (index >= 0) {
            entries[index] = fuelEntry
            entriesState.value = entries.toList()
        }
    }

    override suspend fun deleteFuelEntry(entryId: Long) {
        entries.removeAll { it.id == entryId }
        entriesState.value = entries.toList()
    }

    override suspend fun applyMutation(upsertEntries: List<FuelEntry>, deleteEntryIds: List<Long>) {
        deleteEntryIds.forEach { deleteFuelEntry(it) }
        upsertEntries.forEach { updateFuelEntry(it) }
    }
}

private class FakeTripRepository(
    private val trips: MutableList<Trip> = mutableListOf(),
) : TripRepository {
    private val tripsState = MutableStateFlow(trips.toList())

    override fun observeTripsForCar(carId: Long): Flow<List<Trip>> = tripsState.asStateFlow()

    override fun observeActiveTripForCar(carId: Long): Flow<Trip?> =
        MutableStateFlow(trips.firstOrNull { it.carId == carId && it.status == TripStatus.ACTIVE }).asStateFlow()

    override fun observeTrip(tripId: Long): Flow<Trip?> =
        MutableStateFlow(trips.firstOrNull { it.id == tripId }).asStateFlow()

    override suspend fun getTripsForCar(carId: Long): List<Trip> = trips.filter { it.carId == carId }

    override suspend fun getActiveTripForCar(carId: Long): Trip? =
        trips.firstOrNull { it.carId == carId && it.status == TripStatus.ACTIVE }

    override suspend fun getTrip(tripId: Long): Trip? = trips.firstOrNull { it.id == tripId }

    override suspend fun upsertTrip(trip: Trip): Long {
        val id = if (trip.id == 0L) (trips.maxOfOrNull { it.id } ?: 0L) + 1L else trip.id
        val savedTrip = trip.copy(id = id)
        val index = trips.indexOfFirst { it.id == id }
        if (index >= 0) {
            trips[index] = savedTrip
        } else {
            trips += savedTrip
        }
        tripsState.value = trips.toList()
        return id
    }

    override suspend fun deleteTrip(tripId: Long) {
        trips.removeAll { it.id == tripId }
        tripsState.value = trips.toList()
    }
}

private fun testCar(initialOdometerKm: Long) = Car(
    id = 1,
    name = "Test car",
    tankSizeMilliliters = 55_000,
    initialOdometerKm = initialOdometerKm,
    createdAtEpochMillis = 1,
    updatedAtEpochMillis = 1,
)

private fun testFuelEntry(id: Long, odometerKm: Long) = FuelEntry(
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
    id: Long,
    status: TripStatus,
    startOdometerKm: Long,
) = Trip(
    id = id,
    carId = 1,
    title = "Lapland trip",
    startLocation = "Helsinki",
    endLocation = null,
    startOdometerKm = startOdometerKm,
    endOdometerKm = null,
    startTimeEpochMillis = id,
    endTimeEpochMillis = null,
    status = status,
    createdAtEpochMillis = id,
    updatedAtEpochMillis = id,
)
