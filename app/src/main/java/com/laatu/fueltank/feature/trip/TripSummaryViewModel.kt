package com.laatu.fueltank.feature.trip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.TripBreakMutationError
import com.laatu.fueltank.domain.service.TripBreakMutationException
import com.laatu.fueltank.domain.usecase.DeleteTripBreakUseCase
import com.laatu.fueltank.domain.usecase.EndCurrentTripBreakUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.RenameTripUseCase
import com.laatu.fueltank.domain.usecase.StartTripBreakUseCase
import com.laatu.fueltank.domain.usecase.UpdateTripTimesUseCase
import com.laatu.fueltank.domain.usecase.UpsertTripBreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TripSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    tripRepository: TripRepository,
    tripBreakRepository: TripBreakRepository,
    private val tripSummaryUiMapper: TripSummaryUiMapper,
    private val renameTripUseCase: RenameTripUseCase,
    private val startTripBreakUseCase: StartTripBreakUseCase,
    private val endCurrentTripBreakUseCase: EndCurrentTripBreakUseCase,
    private val upsertTripBreakUseCase: UpsertTripBreakUseCase,
    private val deleteTripBreakUseCase: DeleteTripBreakUseCase,
    private val updateTripTimesUseCase: UpdateTripTimesUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    private val tripId: Long = checkNotNull(savedStateHandle["tripId"])
    private val actionError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TripSummaryUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                flowOf(TripSummaryUiState(title = stringResourceProvider.get(R.string.trip_summary_not_found)))
            } else {
                combine(
                    tripRepository.observeTrip(tripId),
                    fuelEntryRepository.observeEntriesForCar(car.id),
                    tripRepository.observeTripsForCar(car.id),
                    tripBreakRepository.observeBreaksForTrip(tripId),
                    actionError,
                ) { trip, entries, trips, tripBreaks, error ->
                    if (trip == null) {
                        TripSummaryUiState(title = stringResourceProvider.get(R.string.trip_summary_not_found))
                    } else {
                        tripSummaryUiMapper.map(
                            car = car,
                            trip = trip,
                            entries = entries,
                            trips = trips,
                            tripBreaks = tripBreaks,
                            actionError = error,
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripSummaryUiState(),
        )

    fun renameTrip(newTitle: String) {
        viewModelScope.launch {
            renameTripUseCase(
                tripId = tripId,
                title = newTitle,
            )
        }
    }

    fun startBreak() {
        viewModelScope.launch {
            runTripTimeAction {
                startTripBreakUseCase()
            }
        }
    }

    fun endBreak() {
        viewModelScope.launch {
            runTripTimeAction {
                endCurrentTripBreakUseCase()
            }
        }
    }

    fun saveBreak(
        breakId: Long?,
        startInput: String,
        endInput: String,
        note: String,
    ) {
        viewModelScope.launch {
            runTripTimeAction {
                upsertTripBreakUseCase(
                    tripId = tripId,
                    breakId = breakId,
                    startTimeEpochMillis = parseEditableDateTime(startInput),
                    endTimeEpochMillis = endInput.takeIf { it.isNotBlank() }?.let(::parseEditableDateTime),
                    note = note,
                )
            }
        }
    }

    fun deleteBreak(breakId: Long) {
        viewModelScope.launch {
            runTripTimeAction {
                deleteTripBreakUseCase(breakId)
            }
        }
    }

    fun saveTripTimes(
        startInput: String,
        endInput: String,
    ) {
        viewModelScope.launch {
            runTripTimeAction {
                updateTripTimesUseCase(
                    tripId = tripId,
                    startTimeEpochMillis = parseEditableDateTime(startInput),
                    endTimeEpochMillis = endInput.takeIf { it.isNotBlank() }?.let(::parseEditableDateTime),
                )
            }
        }
    }

    fun clearActionError() {
        actionError.value = null
    }

    private suspend fun runTripTimeAction(action: suspend () -> Any?) {
        actionError.value = null
        try {
            action()
        } catch (error: TripBreakMutationException) {
            actionError.value = error.toUiMessage(stringResourceProvider)
        } catch (error: DateTimeParseException) {
            actionError.value = stringResourceProvider.get(R.string.trip_time_error_datetime_format)
        }
    }

    private fun parseEditableDateTime(value: String): Long {
        return LocalDateTime.parse(value.trim(), tripSummaryEditDateTimeFormatter)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}

data class TripSummaryUiState(
    val title: String = "",
    val headerSubtitle: String = "",
    val isActive: Boolean = false,
    val tripDateLabel: String = "",
    val routeLabel: String = "",
    val startDateTimeLabel: String = "",
    val endDateTimeLabel: String = "",
    val startTimeLabel: String = "",
    val endTimeLabel: String = "",
    val startDateLabel: String? = null,
    val endDateLabel: String? = null,
    val showTimeCardDates: Boolean = false,
    val startLocationLabel: String = "",
    val distanceLabel: String = "0 km",
    val fuelBoughtLabel: String = "0.0 L",
    val fuelCostLabel: String = "0.00 EUR",
    val tripStartInput: String = "",
    val tripEndInput: String = "",
    val newBreakStartInput: String = "",
    val newBreakEndInput: String = "",
    val totalElapsedLabel: String = "",
    val totalBreakLabel: String = "",
    val drivingTimeLabel: String = "",
    val averageSpeedIncludingBreaksLabel: String = "",
    val averageSpeedExcludingBreaksLabel: String = "",
    val drivingShare: Float = 0f,
    val hasActiveBreak: Boolean = false,
    val actionError: String? = null,
    val overviewFuelMetricValue: String = "",
    val overviewFuelMetricLabel: String = "",
    val overviewFuelMetricNote: String = "",
    val breaks: List<TripBreakUiModel> = emptyList(),
    val timeline: List<TripTimelineUiItem> = emptyList(),
    val linkedRefuels: List<TripLinkedRefuelUiModel> = emptyList(),
)

data class TripBreakUiModel(
    val id: Long,
    val timeRangeLabel: String,
    val durationLabel: String,
    val note: String,
    val startInput: String,
    val endInput: String,
)

data class TripTimelineUiItem(
    val id: String,
    val type: TripTimelineUiType,
    val title: String,
    val primaryLabel: String,
    val secondaryLabel: String,
    val trailingLabel: String,
    val editableBreakId: Long? = null,
)

enum class TripTimelineUiType {
    TRIP_STARTED,
    BREAK,
    FUEL_ENTRY,
    TRIP_ENDED,
}

data class TripLinkedRefuelUiModel(
    val id: Long,
    val stationLabel: String,
    val dateTimeLabel: String,
    val litersLabel: String,
    val costLabel: String,
)

private fun TripBreakMutationException.toUiMessage(
    stringResourceProvider: StringResourceProvider,
): String = when (error) {
    TripBreakMutationError.TRIP_NOT_FOUND -> stringResourceProvider.get(R.string.trip_time_error_trip_not_found)
    TripBreakMutationError.BREAK_NOT_FOUND -> stringResourceProvider.get(R.string.trip_time_error_break_not_found)
    TripBreakMutationError.ACTIVE_BREAK_EXISTS -> stringResourceProvider.get(R.string.trip_time_error_active_break_exists)
    TripBreakMutationError.NEGATIVE_DURATION -> stringResourceProvider.get(R.string.trip_time_error_negative_duration)
    TripBreakMutationError.OVERLAPPING_BREAKS -> stringResourceProvider.get(R.string.trip_time_error_overlapping_breaks)
    TripBreakMutationError.BREAK_OUTSIDE_TRIP -> stringResourceProvider.get(R.string.trip_time_error_break_outside_trip)
}
