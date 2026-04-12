package com.laatu.fueltank.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.StartTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
class StartTripViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    tripRepository: TripRepository,
    private val currentOdometerService: CurrentOdometerService,
    private val startTripUseCase: StartTripUseCase,
) : ViewModel() {
    private val formState = MutableStateFlow(StartTripFormState())
    private val events = Channel<StartTripEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    val uiState: StateFlow<StartTripUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                combine(formState, flowOf("Not available"), flowOf(false)) { form, odometerLabel, canStart ->
                    form.toUiState(odometerLabel, canStart)
                }
            } else {
                combine(
                    formState,
                    fuelEntryRepository.observeEntriesForCar(car.id),
                    tripRepository.observeTripsForCar(car.id),
                ) { form, entries, trips ->
                    val snapshot = currentOdometerService.resolve(car, entries, trips)
                    form.toUiState(
                        currentOdometerLabel = "${snapshot.odometerKm} km",
                        canStart = trips.none { it.status == TripStatus.ACTIVE },
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StartTripUiState(isLoading = true),
        )

    fun onTitleChange(value: String) = formState.update { it.copy(title = value, errorMessage = null) }
    fun onStartLocationChange(value: String) = formState.update { it.copy(startLocation = value, errorMessage = null) }

    fun submit() {
        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val tripId = startTripUseCase(
                    title = formState.value.title,
                    startLocation = formState.value.startLocation,
                )
                if (tripId > 0) {
                    events.send(StartTripEvent.TripStarted(tripId))
                } else {
                    formState.update { it.copy(isSaving = false, errorMessage = "Create a car first.") }
                }
            } catch (error: IllegalStateException) {
                formState.update { it.copy(isSaving = false, errorMessage = error.message) }
            }
        }
    }
}

data class StartTripUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val startLocation: String = "",
    val currentOdometerLabel: String = "Not available",
    val canStart: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

sealed interface StartTripEvent {
    data class TripStarted(val tripId: Long) : StartTripEvent
}

private data class StartTripFormState(
    val title: String = "",
    val startLocation: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
) {
    fun toUiState(currentOdometerLabel: String, canStart: Boolean) = StartTripUiState(
        title = title,
        startLocation = startLocation,
        currentOdometerLabel = currentOdometerLabel,
        canStart = canStart,
        errorMessage = if (canStart) errorMessage else "Only one trip can be active at a time.",
        isSaving = isSaving,
    )
}
