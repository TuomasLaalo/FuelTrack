package com.laatu.fueltank.feature.fuelentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.usecase.CreateFuelEntryUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AddFuelEntryViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    tripRepository: TripRepository,
    private val currentOdometerService: CurrentOdometerService,
    private val createFuelEntryUseCase: CreateFuelEntryUseCase,
) : ViewModel() {
    private val formState = MutableStateFlow(AddFuelEntryFormState())
    private val showDiscardDialog = MutableStateFlow(false)

    val uiState: StateFlow<AddFuelEntryUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                combine(formState, showDiscardDialog, flowOf(null as Long?)) { form, discardDialogVisible, baseline ->
                    AddFuelEntryUiState(
                        carId = null,
                        baselineOdometerKm = baseline,
                        form = form,
                        computedTotalPriceLabel = form.toComputedTotalPriceLabel(),
                        showDiscardDialog = discardDialogVisible,
                    )
                }
            } else {
                combine(
                    formState,
                    showDiscardDialog,
                    fuelEntryRepository.observeEntriesForCar(car.id),
                    tripRepository.observeTripsForCar(car.id),
                ) { form, discardDialogVisible, entries, trips ->
                    val activeTrip = trips.firstOrNull { it.status == TripStatus.ACTIVE }
                    val currentOdometer = currentOdometerService.resolve(
                        car = car,
                        fuelEntries = entries,
                        trips = trips,
                    )
                    AddFuelEntryUiState(
                        carId = car.id,
                        baselineOdometerKm = currentOdometer.odometerKm,
                        activeTripId = activeTrip?.id,
                        activeTripLabel = activeTrip?.title ?: activeTrip?.startLocation,
                        form = form,
                        computedTotalPriceLabel = form.toComputedTotalPriceLabel(),
                        showDiscardDialog = discardDialogVisible,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AddFuelEntryUiState(),
        )

    private val events = Channel<AddFuelEntryEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    fun onDistanceModeChange(mode: DistanceInputMode) = formState.updateField {
        copy(distanceInputMode = mode, odometerError = null, tripMeterError = null, generalError = null)
    }

    fun onOdometerChange(value: String) = formState.updateField { copy(odometerKm = value, odometerError = null) }
    fun onTripMeterChange(value: String) = formState.updateField { copy(tripMeterKm = value, tripMeterError = null) }
    fun onLitersChange(value: String) = formState.updateField { copy(liters = value, litersError = null, pricePerLiterError = null) }
    fun onPricePerLiterChange(value: String) = formState.updateField { copy(pricePerLiter = value, pricePerLiterError = null) }
    fun onStationBrandChange(value: String) = formState.updateField { copy(stationBrand = value) }
    fun onNoteChange(value: String) = formState.updateField { copy(note = value) }
    fun onFullTankChange(value: Boolean) = formState.updateField { copy(isFullTank = value) }
    fun onAttachToActiveTripChange(value: Boolean) = formState.updateField { copy(attachToActiveTrip = value) }

    fun requestCancel() {
        if (formState.value.isDirty()) {
            showDiscardDialog.value = true
        } else {
            viewModelScope.launch {
                events.send(AddFuelEntryEvent.Cancelled)
            }
        }
    }

    fun dismissDiscardDialog() {
        showDiscardDialog.value = false
    }

    fun confirmDiscard() {
        showDiscardDialog.value = false
        formState.value = AddFuelEntryFormState()
        viewModelScope.launch {
            events.send(AddFuelEntryEvent.Cancelled)
        }
    }

    fun discardEntry() {
        showDiscardDialog.value = false
        formState.value = AddFuelEntryFormState()
        viewModelScope.launch {
            events.send(AddFuelEntryEvent.Cancelled)
        }
    }

    fun submit() {
        val state = uiState.value
        val carId = state.carId ?: run {
            formState.updateField { copy(generalError = "Create a car first") }
            return
        }

        when (val validation = validate(state)) {
            is AddFuelEntryValidationResult.Invalid -> formState.value = validation.state
            is AddFuelEntryValidationResult.Valid -> {
                viewModelScope.launch {
                    formState.updateField { copy(isSaving = true, generalError = null) }
                    createFuelEntryUseCase(
                        carId = carId,
                        distanceInputMode = validation.distanceInputMode,
                        odometerKm = validation.odometerKm,
                        tripMeterKm = validation.tripMeterKm,
                        litersMilliliters = validation.litersMilliliters,
                        totalCostCents = validation.totalCostCents,
                        pricePerLiterMicros = validation.pricePerLiterMicros,
                        stationBrand = state.form.stationBrand,
                        isFullTank = state.form.isFullTank,
                        note = state.form.note,
                        attachToActiveTrip = state.form.attachToActiveTrip,
                    )
                    formState.value = AddFuelEntryFormState()
                    showDiscardDialog.value = false
                    events.send(AddFuelEntryEvent.EntrySaved)
                }
            }
        }
    }

    private fun validate(state: AddFuelEntryUiState): AddFuelEntryValidationResult {
        var form = state.form.copy(
            odometerError = null,
            tripMeterError = null,
            litersError = null,
            pricePerLiterError = null,
            generalError = null,
            isSaving = false,
        )

        val liters = form.liters.parseLocalizedDecimal()
        if (liters == null || liters <= 0.0) {
            form = form.copy(litersError = "Enter fuel amount")
        }

        val pricePerLiter = form.pricePerLiter.parseLocalizedDecimal()
        if (pricePerLiter == null || pricePerLiter <= 0.0) {
            form = form.copy(pricePerLiterError = "Enter price per liter")
        }

        val odometerKm = when (form.distanceInputMode) {
            DistanceInputMode.ODOMETER -> {
                val value = form.odometerKm.toLongOrNull()
                if (value == null || value < 0) {
                    form = form.copy(odometerError = "Enter current odometer")
                    null
                } else {
                    value
                }
            }
            DistanceInputMode.TRIP_METER -> {
                val baseline = state.baselineOdometerKm
                val tripMeterValue = form.tripMeterKm.toLongOrNull()
                when {
                    baseline == null -> {
                        form = form.copy(generalError = "Trip meter mode needs a known odometer baseline")
                        null
                    }
                    tripMeterValue == null || tripMeterValue < 0 -> {
                        form = form.copy(tripMeterError = "Enter trip meter distance")
                        null
                    }
                    else -> baseline + tripMeterValue
                }
            }
        }

        if (form.hasErrors()) {
            return AddFuelEntryValidationResult.Invalid(form)
        }

        val litersMilliliters = (liters!! * 1000).roundToLong()
        val derivedTotalCost = liters * pricePerLiter!!

        return AddFuelEntryValidationResult.Valid(
            distanceInputMode = form.distanceInputMode,
            odometerKm = odometerKm!!,
            tripMeterKm = form.tripMeterKm.toLongOrNull(),
            litersMilliliters = litersMilliliters,
            totalCostCents = derivedTotalCost.toCents(),
            pricePerLiterMicros = pricePerLiter.toMicrosPerLiter(),
        )
    }
}

data class AddFuelEntryUiState(
    val carId: Long? = null,
    val baselineOdometerKm: Long? = null,
    val activeTripId: Long? = null,
    val activeTripLabel: String? = null,
    val form: AddFuelEntryFormState = AddFuelEntryFormState(),
    val computedTotalPriceLabel: String = "0.00 EUR",
    val showDiscardDialog: Boolean = false,
)

data class AddFuelEntryFormState(
    val distanceInputMode: DistanceInputMode = DistanceInputMode.TRIP_METER,
    val odometerKm: String = "",
    val tripMeterKm: String = "",
    val liters: String = "",
    val pricePerLiter: String = "",
    val stationBrand: String = "",
    val note: String = "",
    val isFullTank: Boolean = false,
    val attachToActiveTrip: Boolean = true,
    val odometerError: String? = null,
    val tripMeterError: String? = null,
    val litersError: String? = null,
    val pricePerLiterError: String? = null,
    val generalError: String? = null,
    val isSaving: Boolean = false,
) {
    fun hasErrors(): Boolean =
        odometerError != null || tripMeterError != null || litersError != null || pricePerLiterError != null || generalError != null

    fun isDirty(): Boolean =
        odometerKm.isNotBlank() ||
            tripMeterKm.isNotBlank() ||
            liters.isNotBlank() ||
            pricePerLiter.isNotBlank() ||
            stationBrand.isNotBlank() ||
            note.isNotBlank() ||
            isFullTank ||
            !attachToActiveTrip ||
            distanceInputMode != DistanceInputMode.TRIP_METER
}

sealed interface AddFuelEntryEvent {
    data object EntrySaved : AddFuelEntryEvent
    data object Cancelled : AddFuelEntryEvent
}

private sealed interface AddFuelEntryValidationResult {
    data class Valid(
        val distanceInputMode: DistanceInputMode,
        val odometerKm: Long,
        val tripMeterKm: Long?,
        val litersMilliliters: Long,
        val totalCostCents: Long,
        val pricePerLiterMicros: Long,
    ) : AddFuelEntryValidationResult

    data class Invalid(val state: AddFuelEntryFormState) : AddFuelEntryValidationResult
}
