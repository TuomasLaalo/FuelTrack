package com.laatu.fueltank.feature.trip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.service.TripSummaryService
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.RenameTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val currentOdometerService: CurrentOdometerService,
    private val tripSummaryService: TripSummaryService,
    private val renameTripUseCase: RenameTripUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    private val tripId: Long = checkNotNull(savedStateHandle["tripId"])

    val uiState: StateFlow<TripSummaryUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                flowOf(TripSummaryUiState(title = stringResourceProvider.get(R.string.trip_summary_not_found)))
            } else {
                combine(
                    flowOf(car),
                    tripRepository.observeTrip(tripId),
                    fuelEntryRepository.observeEntriesForCar(car.id),
                    tripRepository.observeTripsForCar(car.id),
                ) { activeCar, trip, entries, trips ->
                    if (trip == null) {
                        TripSummaryUiState(title = stringResourceProvider.get(R.string.trip_summary_not_found))
                    } else {
                        trip.toUiState(
                            car = activeCar,
                            entries = entries,
                            trips = trips,
                            currentOdometerService = currentOdometerService,
                            tripSummaryService = tripSummaryService,
                            stringResourceProvider = stringResourceProvider,
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
}

data class TripSummaryUiState(
    val title: String = "",
    val statusLabel: String = "",
    val statusTone: TripStatusTone = TripStatusTone.Active,
    val headerSubtitle: String = "",
    val isActive: Boolean = false,
    val startDateTimeLabel: String = "",
    val startLocationLabel: String = "",
    val distanceLabel: String = "0 km",
    val fuelBoughtLabel: String = "0.0 L",
    val fuelCostLabel: String = "0.00 €",
    val durationLabel: String = "",
    val consumptionLabel: String = "",
    val showConsumptionValue: Boolean = false,
    val consumptionSupportingMessage: String = "",
    val linkedRefuels: List<TripLinkedRefuelUiModel> = emptyList(),
)

data class TripLinkedRefuelUiModel(
    val id: Long,
    val stationLabel: String,
    val dateTimeLabel: String,
    val litersLabel: String,
    val costLabel: String,
)

enum class TripStatusTone {
    Active,
    Completed,
}

private fun Trip.toUiState(
    car: Car,
    entries: List<FuelEntry>,
    trips: List<Trip>,
    currentOdometerService: CurrentOdometerService,
    tripSummaryService: TripSummaryService,
    stringResourceProvider: StringResourceProvider,
): TripSummaryUiState {
    val locale = stringResourceProvider.currentLocale()
    val linkedEntries = entries.filter { it.tripId == id }.sortedByDescending { it.dateTimeEpochMillis }
    val currentOdometer = currentOdometerService.resolve(car, entries, trips)
    val liveEndOdometer = when (status) {
        TripStatus.ACTIVE -> maxOf(currentOdometer.odometerKm, startOdometerKm)
        TripStatus.COMPLETED -> endOdometerKm ?: startOdometerKm
    }
    val liveTrip = copy(endOdometerKm = liveEndOdometer)
    val summary = tripSummaryService.buildSummary(
        trip = liveTrip,
        linkedEntries = linkedEntries,
    )
    val isActiveTrip = status == TripStatus.ACTIVE
    val startFormatter = DateTimeFormatter
        .ofPattern(stringResourceProvider.get(R.string.trip_summary_start_datetime_format), locale)
        .withZone(ZoneId.systemDefault())
    val refuelFormatter = DateTimeFormatter
        .ofPattern(stringResourceProvider.get(R.string.trip_summary_refuel_datetime_format), locale)
        .withZone(ZoneId.systemDefault())

    return TripSummaryUiState(
        title = title ?: if (isActiveTrip) {
            stringResourceProvider.get(R.string.trip_summary_active_default_title)
        } else {
            stringResourceProvider.get(R.string.trip_summary_default_title)
        },
        statusLabel = if (isActiveTrip) {
            stringResourceProvider.get(R.string.trip_summary_active)
        } else {
            stringResourceProvider.get(R.string.trip_summary_completed)
        },
        statusTone = if (isActiveTrip) TripStatusTone.Active else TripStatusTone.Completed,
        headerSubtitle = if (isActiveTrip) "" else stringResourceProvider.get(R.string.trip_summary_header_subtitle),
        isActive = isActiveTrip,
        startDateTimeLabel = startFormatter.format(Instant.ofEpochMilli(startTimeEpochMillis)),
        startLocationLabel = startLocation ?: stringResourceProvider.get(R.string.trip_summary_start_location_not_set),
        distanceLabel = stringResourceProvider.get(R.string.dashboard_distance_km, summary.distanceKm),
        fuelBoughtLabel = stringResourceProvider.get(
            R.string.unit_liters_short,
            decimalFormat.format(summary.fuelBoughtMilliliters / 1000.0),
        ),
        fuelCostLabel = decimalFormat.format(summary.fuelCostCents / 100.0) + " €",
        durationLabel = buildDurationLabel(
            startTimeEpochMillis = startTimeEpochMillis,
            endTimeEpochMillis = endTimeEpochMillis,
            active = isActiveTrip,
            stringResourceProvider = stringResourceProvider,
        ),
        consumptionLabel = summary.averageConsumptionLitersPer100Km?.let {
            decimalFormat.format(it) + " " + stringResourceProvider.get(R.string.dashboard_consumption_unit)
        } ?: stringResourceProvider.get(R.string.trip_summary_not_enough_refuels),
        showConsumptionValue = summary.averageConsumptionLitersPer100Km != null,
        consumptionSupportingMessage = when (summary.confidence) {
            CalculationConfidence.LOW_CONFIDENCE -> stringResourceProvider.get(R.string.trip_summary_low_confidence)
            CalculationConfidence.HIGH_CONFIDENCE -> stringResourceProvider.get(R.string.trip_summary_high_confidence)
            CalculationConfidence.NOT_AVAILABLE -> stringResourceProvider.get(R.string.trip_summary_no_consumption_data)
        },
        linkedRefuels = linkedEntries.map { entry ->
            TripLinkedRefuelUiModel(
                id = entry.id,
                stationLabel = entry.stationBrand?.ifBlank { null }
                    ?: stringResourceProvider.get(R.string.trip_summary_station_not_set),
                dateTimeLabel = refuelFormatter.format(Instant.ofEpochMilli(entry.dateTimeEpochMillis)),
                litersLabel = stringResourceProvider.get(
                    R.string.unit_liters_short,
                    decimalFormat.format(entry.litersMilliliters / 1000.0),
                ),
                costLabel = decimalFormat.format(entry.totalCostCents / 100.0) + " €",
            )
        },
    )
}

private fun buildDurationLabel(
    startTimeEpochMillis: Long,
    endTimeEpochMillis: Long?,
    active: Boolean,
    stringResourceProvider: StringResourceProvider,
): String {
    val end = endTimeEpochMillis ?: System.currentTimeMillis()
    val duration = Duration.ofMillis((end - startTimeEpochMillis).coerceAtLeast(0L))
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    val base = when {
        hours > 0 -> stringResourceProvider.get(R.string.trip_summary_duration_hours_minutes, hours, minutes)
        else -> stringResourceProvider.get(R.string.trip_summary_duration_minutes, minutes)
    }
    return if (active) {
        stringResourceProvider.get(R.string.trip_summary_duration_running, base)
    } else {
        stringResourceProvider.get(R.string.trip_summary_duration_completed, base)
    }
}

private val decimalFormat = DecimalFormat("0.0#")
