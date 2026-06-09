package com.laatu.fueltank.feature.trip

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.MainDispatcherRule
import com.laatu.fueltank.TestStringResourceProvider
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.service.TripBreakValidationService
import com.laatu.fueltank.domain.service.TripSummaryService
import com.laatu.fueltank.domain.usecase.DeleteTripBreakUseCase
import com.laatu.fueltank.domain.usecase.EndActiveTripUseCase
import com.laatu.fueltank.domain.usecase.EndCurrentTripBreakUseCase
import com.laatu.fueltank.domain.usecase.ObserveActiveTripUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.RenameTripUseCase
import com.laatu.fueltank.domain.usecase.StartTripBreakUseCase
import com.laatu.fueltank.domain.usecase.StartTripUseCase
import com.laatu.fueltank.domain.usecase.UpdateTripTimesUseCase
import com.laatu.fueltank.domain.usecase.UpsertTripBreakUseCase
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
class TripViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startTrip_usesDerivedCurrentOdometerAndEmitsStartedEvent() = runTest {
        val carRepository = FakeTripCarRepository(testCar(initialOdometerKm = 10_000))
        val fuelEntryRepository = FakeTripFuelEntryRepository(
            mutableListOf(testFuelEntry(id = 1, odometerKm = 10_200)),
        )
        val tripRepository = FakeTripRepository(
            mutableListOf(testTrip(id = 4, status = TripStatus.COMPLETED, startOdometerKm = 10_100, endOdometerKm = 10_450)),
        )
        val viewModel = startTripViewModel(carRepository, fuelEntryRepository, tripRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        val event = async { viewModel.eventFlow.first() }
        advanceUntilIdle()

        viewModel.onTitleChange("  Cabin  ")
        viewModel.onStartLocationChange("  Helsinki  ")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val startedTrip = tripRepository.trips.first { it.status == TripStatus.ACTIVE }
        assertThat(startedTrip.startOdometerKm).isEqualTo(10_450)
        assertThat(startedTrip.title).isEqualTo("Cabin")
        assertThat(startedTrip.startLocation).isEqualTo("Helsinki")
        assertThat((event.await() as StartTripEvent.TripStarted).tripId).isEqualTo(startedTrip.id)
        collectJob.cancel()
    }

    @Test
    fun startTrip_withoutStartLocationShowsValidationError() = runTest {
        val carRepository = FakeTripCarRepository(testCar())
        val fuelEntryRepository = FakeTripFuelEntryRepository()
        val tripRepository = FakeTripRepository()
        val viewModel = startTripViewModel(carRepository, fuelEntryRepository, tripRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onTitleChange("Cabin")
        viewModel.submit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("Enter start location")
        assertThat(tripRepository.trips).isEmpty()
        collectJob.cancel()
    }

    @Test
    fun startTrip_whenActiveTripExists_exposesOnlyOneActiveTripError() = runTest {
        val viewModel = startTripViewModel(
            carRepository = FakeTripCarRepository(testCar()),
            fuelEntryRepository = FakeTripFuelEntryRepository(),
            tripRepository = FakeTripRepository(
                mutableListOf(testTrip(id = 1, status = TripStatus.ACTIVE, startOdometerKm = 10_000)),
            ),
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertThat(state.canStart).isFalse()
        assertThat(state.errorMessage).isEqualTo("Only one trip can be active at a time.")
        collectJob.cancel()
    }

    @Test
    fun endTrip_withTripMeterCompletesActiveTripAndEmitsEndedEvent() = runTest {
        val carRepository = FakeTripCarRepository(testCar())
        val tripRepository = FakeTripRepository(
            mutableListOf(testTrip(id = 5, status = TripStatus.ACTIVE, startOdometerKm = 20_000)),
        )
        val tripBreakRepository = FakeTripBreakRepository(
            mutableListOf(testTripBreak(id = 1, tripId = 5, startTimeEpochMillis = 2_000, endTimeEpochMillis = null)),
        )
        val viewModel = endTripViewModel(carRepository, tripRepository, tripBreakRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        val event = async { viewModel.eventFlow.first() }
        advanceUntilIdle()

        viewModel.onTripMeterChange("350")
        viewModel.onEndLocationChange("  Oulu  ")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val endedTrip = tripRepository.trips.single { it.id == 5L }
        assertThat(endedTrip.status).isEqualTo(TripStatus.COMPLETED)
        assertThat(endedTrip.endOdometerKm).isEqualTo(20_350)
        assertThat(endedTrip.endLocation).isEqualTo("Oulu")
        assertThat(tripBreakRepository.breaks.single { it.id == 1L }.endTimeEpochMillis).isNotNull()
        assertThat((event.await() as EndTripEvent.TripEnded).tripId).isEqualTo(5)
        collectJob.cancel()
    }

    @Test
    fun endTrip_withInvalidInputsSetsValidationErrors() = runTest {
        val viewModel = endTripViewModel(
            carRepository = FakeTripCarRepository(testCar()),
            tripRepository = FakeTripRepository(
                mutableListOf(testTrip(id = 5, status = TripStatus.ACTIVE, startOdometerKm = 20_000)),
            ),
            tripBreakRepository = FakeTripBreakRepository(),
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.tripMeterError).isEqualTo("Enter trip distance")

        viewModel.onDistanceModeChange(DistanceInputMode.ODOMETER)
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.odometerError).isEqualTo("Enter current odometer")
        collectJob.cancel()
    }

    @Test
    fun tripSummary_breakActionsStartEndSaveDeleteAndMapErrors() = runTest {
        val carRepository = FakeTripCarRepository(testCar())
        val fuelEntryRepository = FakeTripFuelEntryRepository()
        val tripRepository = FakeTripRepository(
            mutableListOf(testTrip(id = 9, status = TripStatus.ACTIVE, startOdometerKm = 10_000)),
        )
        val tripBreakRepository = FakeTripBreakRepository()
        val viewModel = tripSummaryViewModel(carRepository, fuelEntryRepository, tripRepository, tripBreakRepository, tripId = 9)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.startBreak()
        advanceUntilIdle()
        assertThat(tripBreakRepository.breaks).hasSize(1)
        assertThat(viewModel.uiState.value.hasActiveBreak).isTrue()

        viewModel.startBreak()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.actionError).isEqualTo("End the current break before starting another.")

        viewModel.clearActionError()
        viewModel.endBreak()
        advanceUntilIdle()
        val endedBreak = tripBreakRepository.breaks.single()
        assertThat(endedBreak.endTimeEpochMillis).isNotNull()

        viewModel.saveBreak(
            breakId = null,
            startInput = "bad date",
            endInput = "",
            note = "",
        )
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.actionError).isEqualTo("Choose a valid date and time.")

        viewModel.clearActionError()
        viewModel.saveBreak(
            breakId = null,
            startInput = "2026-04-26 10:00",
            endInput = "2026-04-26 10:30",
            note = " coffee ",
        )
        advanceUntilIdle()
        assertThat(tripBreakRepository.breaks.mapNotNull { it.note }).contains("coffee")

        val savedBreakId = tripBreakRepository.breaks.first { it.note == "coffee" }.id
        viewModel.deleteBreak(savedBreakId)
        advanceUntilIdle()
        assertThat(tripBreakRepository.breaks.any { it.id == savedBreakId }).isFalse()
        collectJob.cancel()
    }

    @Test
    fun tripSummary_saveTripTimesMapsOutsideBreakErrorAndRenameUpdatesTrip() = runTest {
        val carRepository = FakeTripCarRepository(testCar())
        val fuelEntryRepository = FakeTripFuelEntryRepository()
        val tripRepository = FakeTripRepository(
            mutableListOf(
                testTrip(
                    id = 9,
                    status = TripStatus.COMPLETED,
                    startOdometerKm = 10_000,
                    endOdometerKm = 10_200,
                    startTimeEpochMillis = "2026-04-26 09:00".toEpochMillis(),
                    endTimeEpochMillis = "2026-04-26 12:00".toEpochMillis(),
                ),
            ),
        )
        val tripBreakRepository = FakeTripBreakRepository(
            mutableListOf(
                testTripBreak(
                    id = 3,
                    tripId = 9,
                    startTimeEpochMillis = "2026-04-26 10:00".toEpochMillis(),
                    endTimeEpochMillis = "2026-04-26 10:30".toEpochMillis(),
                ),
            ),
        )
        val viewModel = tripSummaryViewModel(carRepository, fuelEntryRepository, tripRepository, tripBreakRepository, tripId = 9)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.saveTripTimes(
            startInput = "2026-04-26 10:15",
            endInput = "2026-04-26 11:00",
        )
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.actionError).isEqualTo("Break time must stay inside the trip time range.")

        viewModel.renameTrip("  Updated trip  ")
        advanceUntilIdle()
        assertThat(tripRepository.trips.single { it.id == 9L }.title).isEqualTo("Updated trip")
        collectJob.cancel()
    }

    private fun startTripViewModel(
        carRepository: FakeTripCarRepository,
        fuelEntryRepository: FakeTripFuelEntryRepository,
        tripRepository: FakeTripRepository,
    ) = StartTripViewModel(
        observePrimaryCarUseCase = ObservePrimaryCarUseCase(carRepository),
        fuelEntryRepository = fuelEntryRepository,
        tripRepository = tripRepository,
        currentOdometerService = CurrentOdometerService(),
        startTripUseCase = StartTripUseCase(
            carRepository = carRepository,
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = tripRepository,
            currentOdometerService = CurrentOdometerService(),
        ),
        stringResourceProvider = TestStringResourceProvider(),
    )

    private fun endTripViewModel(
        carRepository: FakeTripCarRepository,
        tripRepository: FakeTripRepository,
        tripBreakRepository: FakeTripBreakRepository,
    ) = EndTripViewModel(
        observeActiveTripUseCase = ObserveActiveTripUseCase(
            observePrimaryCarUseCase = ObservePrimaryCarUseCase(carRepository),
            tripRepository = tripRepository,
        ),
        endActiveTripUseCase = EndActiveTripUseCase(
            carRepository = carRepository,
            tripRepository = tripRepository,
            tripBreakRepository = tripBreakRepository,
        ),
        stringResourceProvider = TestStringResourceProvider(),
    )

    private fun tripSummaryViewModel(
        carRepository: FakeTripCarRepository,
        fuelEntryRepository: FakeTripFuelEntryRepository,
        tripRepository: FakeTripRepository,
        tripBreakRepository: FakeTripBreakRepository,
        tripId: Long,
    ): TripSummaryViewModel {
        val validationService = TripBreakValidationService()
        return TripSummaryViewModel(
            savedStateHandle = SavedStateHandle(mapOf("tripId" to tripId)),
            observePrimaryCarUseCase = ObservePrimaryCarUseCase(carRepository),
            fuelEntryRepository = fuelEntryRepository,
            tripRepository = tripRepository,
            tripBreakRepository = tripBreakRepository,
            tripSummaryUiMapper = TripSummaryUiMapper(
                currentOdometerService = CurrentOdometerService(),
                tripSummaryService = TripSummaryService(),
                stringResourceProvider = TestStringResourceProvider(),
            ),
            renameTripUseCase = RenameTripUseCase(tripRepository),
            startTripBreakUseCase = StartTripBreakUseCase(
                carRepository = carRepository,
                tripRepository = tripRepository,
                tripBreakRepository = tripBreakRepository,
                tripBreakValidationService = validationService,
            ),
            endCurrentTripBreakUseCase = EndCurrentTripBreakUseCase(
                carRepository = carRepository,
                tripRepository = tripRepository,
                tripBreakRepository = tripBreakRepository,
                tripBreakValidationService = validationService,
            ),
            upsertTripBreakUseCase = UpsertTripBreakUseCase(
                tripRepository = tripRepository,
                tripBreakRepository = tripBreakRepository,
                tripBreakValidationService = validationService,
            ),
            deleteTripBreakUseCase = DeleteTripBreakUseCase(tripBreakRepository),
            updateTripTimesUseCase = UpdateTripTimesUseCase(
                tripRepository = tripRepository,
                tripBreakRepository = tripBreakRepository,
            ),
            stringResourceProvider = TestStringResourceProvider(),
        )
    }
}

private class FakeTripCarRepository(
    primaryCar: Car? = testCar(),
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
    private val entries: MutableList<FuelEntry> = mutableListOf(),
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
    val trips: MutableList<Trip> = mutableListOf(),
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
        val id = if (trip.id == 0L) (trips.maxOfOrNull { it.id } ?: 0L) + 1L else trip.id
        val savedTrip = trip.copy(id = id)
        val index = trips.indexOfFirst { it.id == id }
        if (index >= 0) {
            trips[index] = savedTrip
        } else {
            trips += savedTrip
        }
        state.value = trips.toList()
        return id
    }

    override suspend fun deleteTrip(tripId: Long) {
        trips.removeAll { it.id == tripId }
        state.value = trips.toList()
    }
}

private class FakeTripBreakRepository(
    val breaks: MutableList<TripBreak> = mutableListOf(),
) : TripBreakRepository {
    private val state = MutableStateFlow(breaks.toList())

    override fun observeBreaksForTrip(tripId: Long): Flow<List<TripBreak>> = state.asStateFlow()

    override suspend fun getBreaksForTrip(tripId: Long): List<TripBreak> =
        breaks.filter { it.tripId == tripId }.sortedWith(compareBy<TripBreak> { it.startTimeEpochMillis }.thenBy { it.id })

    override suspend fun getBreak(breakId: Long): TripBreak? = breaks.firstOrNull { it.id == breakId }

    override suspend fun getActiveBreakForTrip(tripId: Long): TripBreak? =
        breaks.filter { it.tripId == tripId && it.endTimeEpochMillis == null }.maxByOrNull { it.startTimeEpochMillis }

    override suspend fun upsertBreak(tripBreak: TripBreak): Long {
        val id = if (tripBreak.id == 0L) (breaks.maxOfOrNull { it.id } ?: 0L) + 1L else tripBreak.id
        val savedBreak = tripBreak.copy(id = id)
        val index = breaks.indexOfFirst { it.id == id }
        if (index >= 0) {
            breaks[index] = savedBreak
        } else {
            breaks += savedBreak
        }
        state.value = breaks.toList()
        return id
    }

    override suspend fun deleteBreak(breakId: Long) {
        breaks.removeAll { it.id == breakId }
        state.value = breaks.toList()
    }
}

private fun testCar(initialOdometerKm: Long = 10_000) = Car(
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
    endOdometerKm: Long? = null,
    startTimeEpochMillis: Long = 1_000,
    endTimeEpochMillis: Long? = null,
) = Trip(
    id = id,
    carId = 1,
    title = "Trip",
    startLocation = "Helsinki",
    endLocation = null,
    startOdometerKm = startOdometerKm,
    endOdometerKm = endOdometerKm,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = status,
    createdAtEpochMillis = startTimeEpochMillis,
    updatedAtEpochMillis = startTimeEpochMillis,
)

private fun testTripBreak(
    id: Long,
    tripId: Long,
    startTimeEpochMillis: Long,
    endTimeEpochMillis: Long?,
) = TripBreak(
    id = id,
    tripId = tripId,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    note = null,
    createdAtEpochMillis = startTimeEpochMillis,
    updatedAtEpochMillis = endTimeEpochMillis ?: startTimeEpochMillis,
)

private fun String.toEpochMillis(): Long =
    java.time.LocalDateTime.parse(this, tripSummaryEditDateTimeFormatter)
        .atZone(java.time.ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
