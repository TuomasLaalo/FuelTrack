package com.laatu.fueltank.feature.fuelentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.service.FuelEntryMutationError
import com.laatu.fueltank.domain.service.FuelEntryMutationException
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.UpdateFuelEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
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
class EditFuelEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    private val updateFuelEntryUseCase: UpdateFuelEntryUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    private val entryId: Long = checkNotNull(savedStateHandle["entryId"])
    private val formState = MutableStateFlow(AddFuelEntryFormState())
    private val isInitialized = MutableStateFlow(false)

    val uiState: StateFlow<EditFuelEntryUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                combine(formState, flowOf(false)) { form, _ ->
                    EditFuelEntryUiState(
                        formUiState = AddFuelEntryUiState(
                            form = form,
                            computedTotalPriceLabel = form.toComputedTotalPriceLabel(stringResourceProvider),
                        ),
                    )
                }
            } else {
                combine(
                    formState,
                    isInitialized,
                    fuelEntryRepository.observeEntriesForCar(car.id),
                ) { form, initialized, entries ->
                    val chronologicalEntries = entries.sortedChronologically()
                    val target = chronologicalEntries.firstOrNull { it.id == entryId }

                    if (!initialized && target != null) {
                        formState.value = target.toFormState()
                        isInitialized.value = true
                    }

                    val effectiveForm = if (!initialized && target != null) target.toFormState() else form
                    val baseline = chronologicalEntries
                        .indexOfFirst { it.id == entryId }
                        .takeIf { it >= 0 }
                        ?.let { index ->
                            if (index == 0) car.initialOdometerKm else chronologicalEntries[index - 1].odometerKm
                        } ?: car.initialOdometerKm

                    EditFuelEntryUiState(
                        formUiState = AddFuelEntryUiState(
                            carId = car.id,
                            baselineOdometerKm = baseline,
                            form = effectiveForm,
                            computedTotalPriceLabel = effectiveForm.toComputedTotalPriceLabel(stringResourceProvider),
                        ),
                        canEdit = target != null,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EditFuelEntryUiState(isLoading = true),
        )

    private val events = Channel<EditFuelEntryEvent>(Channel.BUFFERED)
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

    fun submit() {
        val state = uiState.value
        if (!state.canEdit) return
        val addState = state.formUiState
        val carId = addState.carId ?: return

        when (val validation = validate(addState)) {
            is EditFuelEntryValidationResult.Invalid -> formState.value = validation.state
            is EditFuelEntryValidationResult.Valid -> {
                viewModelScope.launch {
                    formState.updateField { copy(isSaving = true, generalError = null) }
                    try {
                        updateFuelEntryUseCase(
                            entryId = entryId,
                            carId = carId,
                            distanceInputMode = validation.distanceInputMode,
                            odometerKm = validation.odometerKm,
                            tripMeterKm = validation.tripMeterKm,
                            litersMilliliters = validation.litersMilliliters,
                            totalCostCents = validation.totalCostCents,
                            pricePerLiterMicros = validation.pricePerLiterMicros,
                            stationBrand = addState.form.stationBrand,
                            isFullTank = addState.form.isFullTank,
                            note = addState.form.note,
                        )
                        events.send(EditFuelEntryEvent.EntryUpdated)
                    } catch (error: FuelEntryMutationException) {
                        formState.updateField {
                            copy(
                                isSaving = false,
                                generalError = error.toUiMessage(stringResourceProvider),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun validate(state: AddFuelEntryUiState): EditFuelEntryValidationResult {
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
            form = form.copy(litersError = stringResourceProvider.get(R.string.fuel_entry_error_fuel_amount))
        }
        val pricePerLiter = form.pricePerLiter.parseLocalizedDecimal()
        if (pricePerLiter == null || pricePerLiter <= 0.0) {
            form = form.copy(pricePerLiterError = stringResourceProvider.get(R.string.fuel_entry_error_price_per_unit))
        }

        val odometerKm = when (form.distanceInputMode) {
            DistanceInputMode.ODOMETER -> {
                val value = form.odometerKm.toLongOrNull()
                if (value == null || value < 0) {
                    form = form.copy(odometerError = stringResourceProvider.get(R.string.fuel_entry_error_current_odometer))
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
                        form = form.copy(generalError = stringResourceProvider.get(R.string.fuel_entry_error_trip_meter_baseline))
                        null
                    }
                    tripMeterValue == null || tripMeterValue < 0 -> {
                        form = form.copy(tripMeterError = stringResourceProvider.get(R.string.fuel_entry_error_trip_meter_distance))
                        null
                    }
                    else -> baseline + tripMeterValue
                }
            }
        }

        if (form.hasErrors()) return EditFuelEntryValidationResult.Invalid(form)

        return EditFuelEntryValidationResult.Valid(
            distanceInputMode = form.distanceInputMode,
            odometerKm = odometerKm!!,
            tripMeterKm = form.tripMeterKm.toLongOrNull(),
            litersMilliliters = (liters!! * 1000).roundToLong(),
            totalCostCents = (liters * pricePerLiter!!).toCents(),
            pricePerLiterMicros = pricePerLiter.toMicrosPerLiter(),
        )
    }
}

data class EditFuelEntryUiState(
    val isLoading: Boolean = false,
    val formUiState: AddFuelEntryUiState = AddFuelEntryUiState(),
    val canEdit: Boolean = false,
)

sealed interface EditFuelEntryEvent {
    data object EntryUpdated : EditFuelEntryEvent
}

private sealed interface EditFuelEntryValidationResult {
    data class Valid(
        val distanceInputMode: DistanceInputMode,
        val odometerKm: Long,
        val tripMeterKm: Long?,
        val litersMilliliters: Long,
        val totalCostCents: Long,
        val pricePerLiterMicros: Long,
    ) : EditFuelEntryValidationResult

    data class Invalid(val state: AddFuelEntryFormState) : EditFuelEntryValidationResult
}

private fun com.laatu.fueltank.domain.model.FuelEntry.toFormState(): AddFuelEntryFormState = AddFuelEntryFormState(
    distanceInputMode = distanceInputMode,
    odometerKm = odometerKm.toString(),
    tripMeterKm = tripMeterKm?.toString().orEmpty(),
    liters = decimalFormatter.format(litersMilliliters / 1000.0),
    pricePerLiter = decimalFormatter.format(pricePerLiterMicros / 1_000_000.0),
    stationBrand = stationBrand.orEmpty(),
    note = note.orEmpty(),
    isFullTank = isFullTank,
)

private val decimalFormatter = DecimalFormat(
    "0.##",
    DecimalFormatSymbols.getInstance(Locale.getDefault()),
)

private fun List<com.laatu.fueltank.domain.model.FuelEntry>.sortedChronologically() = sortedWith(
    compareBy<com.laatu.fueltank.domain.model.FuelEntry> { it.dateTimeEpochMillis }
        .thenBy { it.createdAtEpochMillis }
        .thenBy { it.id }
)

private fun FuelEntryMutationException.toUiMessage(
    stringResourceProvider: StringResourceProvider,
): String = when (error) {
    FuelEntryMutationError.ENTRY_NOT_FOUND -> stringResourceProvider.get(R.string.fuel_entry_error_entry_not_found)
    FuelEntryMutationError.ODOMETER_MOVES_BACKWARDS -> stringResourceProvider.get(R.string.fuel_entry_error_odometer_backwards)
    FuelEntryMutationError.TRIP_METER_DISTANCE_MISSING -> stringResourceProvider.get(R.string.fuel_entry_error_trip_meter_missing_distance)
    FuelEntryMutationError.TRIP_METER_DISTANCE_NEGATIVE -> stringResourceProvider.get(R.string.fuel_entry_error_trip_meter_negative_distance)
}
