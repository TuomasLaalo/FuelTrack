package com.laatu.fueltank.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.usecase.EndActiveTripUseCase
import com.laatu.fueltank.domain.usecase.ObserveActiveTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EndTripViewModel @Inject constructor(
    observeActiveTripUseCase: ObserveActiveTripUseCase,
    private val endActiveTripUseCase: EndActiveTripUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    private val formState = MutableStateFlow(EndTripFormState())
    private val events = Channel<EndTripEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    val uiState: StateFlow<EndTripUiState> = combine(
        formState,
        observeActiveTripUseCase(),
    ) { form, activeTrip ->
        EndTripUiState(
            tripTitle = activeTrip?.title ?: stringResourceProvider.get(R.string.trip_summary_active_default_title),
            startOdometerLabel = activeTrip?.startOdometerKm?.let {
                stringResourceProvider.get(R.string.dashboard_distance_km, it)
            } ?: stringResourceProvider.get(R.string.dashboard_not_available),
            distanceInputMode = form.distanceInputMode,
            odometerKm = form.odometerKm,
            tripMeterKm = form.tripMeterKm,
            endLocation = form.endLocation,
            odometerError = form.odometerError,
            tripMeterError = form.tripMeterError,
            errorMessage = form.errorMessage ?: if (activeTrip == null) {
                stringResourceProvider.get(R.string.end_trip_no_active)
            } else {
                null
            },
            isSaving = form.isSaving,
            canEnd = activeTrip != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EndTripUiState(),
    )

    fun onDistanceModeChange(value: DistanceInputMode) = formState.update {
        it.copy(distanceInputMode = value, odometerError = null, tripMeterError = null, errorMessage = null)
    }

    fun onOdometerChange(value: String) = formState.update { it.copy(odometerKm = value, odometerError = null) }
    fun onTripMeterChange(value: String) = formState.update { it.copy(tripMeterKm = value, tripMeterError = null) }
    fun onEndLocationChange(value: String) = formState.update { it.copy(endLocation = value, errorMessage = null) }

    fun submit() {
        val form = formState.value.copy(
            odometerError = null,
            tripMeterError = null,
            errorMessage = null,
            isSaving = false,
        )
        val validatedForm = when (form.distanceInputMode) {
            DistanceInputMode.ODOMETER -> {
                if (form.odometerKm.toLongOrNull() == null) {
                    form.copy(odometerError = stringResourceProvider.get(R.string.fuel_entry_error_current_odometer))
                } else {
                    form
                }
            }
            DistanceInputMode.TRIP_METER -> {
                if (form.tripMeterKm.toLongOrNull() == null) {
                    form.copy(tripMeterError = stringResourceProvider.get(R.string.end_trip_error_trip_distance))
                } else {
                    form
                }
            }
        }

        if (validatedForm.odometerError != null || validatedForm.tripMeterError != null) {
            formState.value = validatedForm
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val tripId = endActiveTripUseCase(
                    distanceInputMode = validatedForm.distanceInputMode,
                    odometerKm = validatedForm.odometerKm.toLongOrNull(),
                    tripMeterKm = validatedForm.tripMeterKm.toLongOrNull(),
                    endLocation = validatedForm.endLocation,
                )
                if (tripId > 0) {
                    events.send(EndTripEvent.TripEnded(tripId))
                } else {
                    formState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = stringResourceProvider.get(R.string.end_trip_no_active),
                        )
                    }
                }
            } catch (error: IllegalStateException) {
                formState.update { it.copy(isSaving = false, errorMessage = error.message) }
            }
        }
    }
}

data class EndTripUiState(
    val tripTitle: String = "",
    val startOdometerLabel: String = "",
    val distanceInputMode: DistanceInputMode = DistanceInputMode.TRIP_METER,
    val odometerKm: String = "",
    val tripMeterKm: String = "",
    val endLocation: String = "",
    val odometerError: String? = null,
    val tripMeterError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val canEnd: Boolean = false,
)

sealed interface EndTripEvent {
    data class TripEnded(val tripId: Long) : EndTripEvent
}

private data class EndTripFormState(
    val distanceInputMode: DistanceInputMode = DistanceInputMode.TRIP_METER,
    val odometerKm: String = "",
    val tripMeterKm: String = "",
    val endLocation: String = "",
    val odometerError: String? = null,
    val tripMeterError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)
