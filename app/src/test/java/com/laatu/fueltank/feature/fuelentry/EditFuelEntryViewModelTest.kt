package com.laatu.fueltank.feature.fuelentry

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.MainDispatcherRule
import com.laatu.fueltank.TestStringResourceProvider
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.service.FuelEntryMutationPlanner
import com.laatu.fueltank.domain.usecase.DeleteFuelEntryUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.UpdateFuelEntryUseCase
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
class EditFuelEntryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun existingEntry_populatesFormAndBaselineFromPreviousEntry() = runTest {
        val viewModel = viewModel(
            entryId = 2,
            fuelEntryRepository = FakeEditFuelEntryRepository(
                mutableListOf(
                    testFuelEntry(id = 1, odometerKm = 10_100),
                    testFuelEntry(
                        id = 2,
                        odometerKm = 10_250,
                        distanceInputMode = DistanceInputMode.TRIP_METER,
                        tripMeterKm = 150,
                        litersMilliliters = 42_500,
                        pricePerLiterMicros = 1_789_000,
                        stationBrand = "Neste",
                        note = "Before trip",
                        isFullTank = true,
                    ),
                ),
            ),
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val form = state.formUiState.form
        assertThat(state.canEdit).isTrue()
        assertThat(state.formUiState.baselineOdometerKm).isEqualTo(10_100)
        assertThat(form.distanceInputMode).isEqualTo(DistanceInputMode.TRIP_METER)
        assertThat(form.tripMeterKm).isEqualTo("150")
        assertThat(form.liters.parseLocalizedDecimal()).isEqualTo(42.5)
        assertThat(form.pricePerLiter.parseLocalizedDecimal()).isEqualTo(1.79)
        assertThat(form.stationBrand).isEqualTo("Neste")
        assertThat(form.note).isEqualTo("Before trip")
        assertThat(form.isFullTank).isTrue()
        collectJob.cancel()
    }

    @Test
    fun submitWithInvalidFields_setsLocalizedErrorsAndDoesNotUpdate() = runTest {
        val fuelEntryRepository = FakeEditFuelEntryRepository(
            mutableListOf(testFuelEntry(id = 2, odometerKm = 10_250)),
        )
        val viewModel = viewModel(entryId = 2, fuelEntryRepository = fuelEntryRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onDistanceModeChange(DistanceInputMode.ODOMETER)
        viewModel.onOdometerChange("")
        viewModel.onLitersChange("")
        viewModel.onPricePerLiterChange("")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val form = viewModel.uiState.value.formUiState.form
        assertThat(form.odometerError).isEqualTo("Enter current odometer")
        assertThat(form.litersError).isEqualTo("Enter fuel amount")
        assertThat(form.pricePerLiterError).isEqualTo("Enter price per liter")
        assertThat(fuelEntryRepository.lastAppliedEntries).isEmpty()
        collectJob.cancel()
    }

    @Test
    fun submitWithTripMeterEdit_updatesEntryAndRecalculatesFollowingTripMeterEntries() = runTest {
        val fuelEntryRepository = FakeEditFuelEntryRepository(
            mutableListOf(
                testFuelEntry(id = 1, odometerKm = 10_100),
                testFuelEntry(
                    id = 2,
                    odometerKm = 10_250,
                    distanceInputMode = DistanceInputMode.TRIP_METER,
                    tripMeterKm = 150,
                ),
                testFuelEntry(
                    id = 3,
                    odometerKm = 10_300,
                    distanceInputMode = DistanceInputMode.TRIP_METER,
                    tripMeterKm = 50,
                ),
            ),
        )
        val viewModel = viewModel(entryId = 2, fuelEntryRepository = fuelEntryRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        val updatedEvent = async { viewModel.eventFlow.first() }
        advanceUntilIdle()

        viewModel.onTripMeterChange("200")
        viewModel.onLitersChange("45")
        viewModel.onPricePerLiterChange("1.7")
        viewModel.onStationBrandChange("  Shell  ")
        viewModel.onNoteChange("  Corrected  ")
        viewModel.onFullTankChange(true)
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertThat(fuelEntryRepository.lastAppliedEntries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_100L,
            2L to 10_300L,
            3L to 10_350L,
        ).inOrder()
        val updatedEntry = fuelEntryRepository.entries.first { it.id == 2L }
        assertThat(updatedEntry.tripMeterKm).isEqualTo(200)
        assertThat(updatedEntry.litersMilliliters).isEqualTo(45_000)
        assertThat(updatedEntry.totalCostCents).isEqualTo(7_650)
        assertThat(updatedEntry.pricePerLiterMicros).isEqualTo(1_700_000)
        assertThat(updatedEntry.stationBrand).isEqualTo("Shell")
        assertThat(updatedEntry.note).isEqualTo("Corrected")
        assertThat(updatedEntry.isFullTank).isTrue()
        assertThat(updatedEvent.await()).isEqualTo(EditFuelEntryEvent.EntryUpdated)
        collectJob.cancel()
    }

    @Test
    fun submitWhenMutationFails_mapsTypedErrorToGeneralError() = runTest {
        val fuelEntryRepository = FakeEditFuelEntryRepository(
            mutableListOf(
                testFuelEntry(id = 1, odometerKm = 10_100),
                testFuelEntry(id = 2, odometerKm = 10_250),
            ),
        )
        val viewModel = viewModel(entryId = 2, fuelEntryRepository = fuelEntryRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onOdometerChange("10050")
        viewModel.onLitersChange("40")
        viewModel.onPricePerLiterChange("1.5")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val form = viewModel.uiState.value.formUiState.form
        assertThat(form.generalError).isEqualTo("Odometer moves backwards")
        assertThat(form.isSaving).isFalse()
        collectJob.cancel()
    }

    @Test
    fun confirmDelete_deletesEntryRecalculatesFollowingEntriesAndEmitsDeletedEvent() = runTest {
        val fuelEntryRepository = FakeEditFuelEntryRepository(
            mutableListOf(
                testFuelEntry(id = 1, odometerKm = 10_100),
                testFuelEntry(
                    id = 2,
                    odometerKm = 10_250,
                    distanceInputMode = DistanceInputMode.TRIP_METER,
                    tripMeterKm = 150,
                ),
                testFuelEntry(
                    id = 3,
                    odometerKm = 10_350,
                    distanceInputMode = DistanceInputMode.TRIP_METER,
                    tripMeterKm = 100,
                ),
            ),
        )
        val viewModel = viewModel(entryId = 2, fuelEntryRepository = fuelEntryRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        val deletedEvent = async { viewModel.eventFlow.first() }
        advanceUntilIdle()

        viewModel.requestDelete()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.showDeleteDialog).isTrue()

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertThat(fuelEntryRepository.lastAppliedDeleteIds).containsExactly(2L)
        assertThat(fuelEntryRepository.entries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_100L,
            3L to 10_200L,
        ).inOrder()
        assertThat(viewModel.uiState.value.showDeleteDialog).isFalse()
        assertThat(deletedEvent.await()).isEqualTo(EditFuelEntryEvent.EntryDeleted)
        collectJob.cancel()
    }

    private fun viewModel(
        entryId: Long,
        fuelEntryRepository: FakeEditFuelEntryRepository,
        carRepository: FakeEditCarRepository = FakeEditCarRepository(testCar()),
    ): EditFuelEntryViewModel =
        EditFuelEntryViewModel(
            savedStateHandle = SavedStateHandle(mapOf("entryId" to entryId)),
            observePrimaryCarUseCase = ObservePrimaryCarUseCase(carRepository),
            fuelEntryRepository = fuelEntryRepository,
            deleteFuelEntryUseCase = DeleteFuelEntryUseCase(
                carRepository = carRepository,
                fuelEntryRepository = fuelEntryRepository,
                fuelEntryMutationPlanner = FuelEntryMutationPlanner(),
            ),
            updateFuelEntryUseCase = UpdateFuelEntryUseCase(
                carRepository = carRepository,
                fuelEntryRepository = fuelEntryRepository,
                fuelEntryMutationPlanner = FuelEntryMutationPlanner(),
            ),
            stringResourceProvider = TestStringResourceProvider(),
        )
}

private class FakeEditCarRepository(
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

private class FakeEditFuelEntryRepository(
    val entries: MutableList<FuelEntry>,
) : FuelEntryRepository {
    private val entriesState = MutableStateFlow(entries.toList())

    var lastAppliedEntries: List<FuelEntry> = emptyList()
        private set
    var lastAppliedDeleteIds: List<Long> = emptyList()
        private set

    override fun observeEntriesForCar(carId: Long): Flow<List<FuelEntry>> = entriesState.asStateFlow()

    override suspend fun getEntriesForCar(carId: Long): List<FuelEntry> =
        entries.filter { it.carId == carId }

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
        deleteEntryIds.forEach { deleteFuelEntry(it) }
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
        entriesState.value = entries.toList()
    }
}

private fun testCar() = Car(
    id = 1,
    name = "Test car",
    tankSizeMilliliters = 55_000,
    initialOdometerKm = 10_000,
    createdAtEpochMillis = 1,
    updatedAtEpochMillis = 1,
)

private fun testFuelEntry(
    id: Long,
    odometerKm: Long,
    distanceInputMode: DistanceInputMode = DistanceInputMode.ODOMETER,
    tripMeterKm: Long? = null,
    litersMilliliters: Long = 40_000,
    totalCostCents: Long = 6_000,
    pricePerLiterMicros: Long = 1_500_000,
    stationBrand: String? = null,
    note: String? = null,
    isFullTank: Boolean = false,
) = FuelEntry(
    id = id,
    carId = 1,
    tripId = null,
    dateTimeEpochMillis = id,
    distanceInputMode = distanceInputMode,
    odometerKm = odometerKm,
    tripMeterKm = tripMeterKm,
    litersMilliliters = litersMilliliters,
    totalCostCents = totalCostCents,
    pricePerLiterMicros = pricePerLiterMicros,
    stationBrand = stationBrand,
    isFullTank = isFullTank,
    note = note,
    createdAtEpochMillis = id,
    updatedAtEpochMillis = id,
)
