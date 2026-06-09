package com.laatu.fueltank.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.core.settings.AppLanguage
import com.laatu.fueltank.core.settings.AppLanguageManager
import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.usecase.BuildDashboardStatsUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    tripRepository: TripRepository,
    private val currentOdometerService: CurrentOdometerService,
    private val buildDashboardStatsUseCase: BuildDashboardStatsUseCase,
    private val stringResourceProvider: StringResourceProvider,
    private val appLanguageManager: AppLanguageManager,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                combine(flowOf(DashboardUiState()), appLanguageManager.currentLanguage) { state, language ->
                    state.copy(currentLanguage = language)
                }
            } else {
                combine(
                    flowOf(car),
                    fuelEntryRepository.observeEntriesForCar(car.id),
                    tripRepository.observeTripsForCar(car.id),
                    appLanguageManager.currentLanguage,
                    minuteTicker,
                ) { activeCar, entries, trips, language, now ->
                    toUiState(activeCar, entries, trips, language, now)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(isLoading = true),
        )

    fun setAppLanguage(language: AppLanguage) {
        appLanguageManager.setLanguage(language)
    }

    private fun toUiState(
        car: Car,
        entries: List<FuelEntry>,
        trips: List<Trip>,
        currentLanguage: AppLanguage,
        nowEpochMillis: Long,
    ): DashboardUiState {
        val currentOdometer = currentOdometerService.resolve(car, entries, trips)
        val stats = buildDashboardStatsUseCase(entries)
        val activeTrip = trips.firstOrNull { it.status == TripStatus.ACTIVE }
        val latestEntry = entries.firstOrNull()
        val locale = stringResourceProvider.currentLocale()

        return DashboardUiState(
            carName = car.name,
            currentOdometerLabel = stringResourceProvider.get(R.string.dashboard_distance_km, currentOdometer.odometerKm),
            totalFuelCostLabel = stats.totalCostCents.toEuroString(),
            totalFuelBoughtLabel = stats.totalLitersMilliliters.toLitersString(),
            totalDistanceLabel = stats.reliableDistanceKm.takeIf { it > 0 }
                ?.let { stringResourceProvider.get(R.string.dashboard_distance_km, it) }
                ?: stringResourceProvider.get(R.string.dashboard_no_reliable_distance),
            heroConsumptionValue = stats.averageConsumptionLitersPer100Km?.let { decimalFormat.format(it) } ?: "--",
            heroConsumptionUnit = if (stats.averageConsumptionLitersPer100Km != null) {
                stringResourceProvider.get(R.string.dashboard_consumption_unit)
            } else {
                stringResourceProvider.get(R.string.dashboard_waiting_for_reliable_fills)
            },
            heroTrendMessage = when (stats.confidence) {
                CalculationConfidence.HIGH_CONFIDENCE -> stringResourceProvider.get(R.string.dashboard_reliable_full_to_full)
                CalculationConfidence.LOW_CONFIDENCE -> stringResourceProvider.get(R.string.dashboard_low_confidence_estimate)
                CalculationConfidence.NOT_AVAILABLE -> stringResourceProvider.get(R.string.dashboard_unlock_reliable_consumption)
            },
            currentLanguage = currentLanguage,
            activeTrip = activeTrip?.toUiModel(stringResourceProvider, nowEpochMillis),
            latestRefuel = latestEntry?.toLatestRefuelCard(stringResourceProvider, locale),
            utilityCard = DashboardUtilityUiModel(
                title = "",
                subtitle = "",
                actionLabel = "",
            ),
        )
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val carName: String = "",
    val currentOdometerLabel: String = "",
    val totalFuelCostLabel: String = "0.00 €",
    val totalFuelBoughtLabel: String = "0.00 L",
    val totalDistanceLabel: String = "",
    val heroConsumptionValue: String = "--",
    val heroConsumptionUnit: String = "",
    val heroTrendMessage: String = "",
    val currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    val activeTrip: DashboardActiveTripUiModel? = null,
    val latestRefuel: DashboardLatestRefuelUiModel? = null,
    val utilityCard: DashboardUtilityUiModel? = null,
)

data class DashboardActiveTripUiModel(
    val id: Long,
    val title: String,
    val locationLabel: String,
    val elapsedLabel: String,
)

data class DashboardLatestRefuelUiModel(
    val dateLabel: String,
    val totalCostLabel: String,
    val litersLabel: String,
    val pricePerLiterLabel: String,
)

data class DashboardUtilityUiModel(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
)

private fun FuelEntry.toLatestRefuelCard(
    stringResourceProvider: StringResourceProvider,
    locale: Locale,
): DashboardLatestRefuelUiModel {
    val formatter = DateTimeFormatter
        .ofPattern(stringResourceProvider.get(R.string.dashboard_latest_refuel_date_format), locale)
        .withZone(ZoneId.systemDefault())
    return DashboardLatestRefuelUiModel(
        dateLabel = formatter.format(Instant.ofEpochMilli(dateTimeEpochMillis)),
        totalCostLabel = totalCostCents.toEuroString(),
        litersLabel = stringResourceProvider.get(
            R.string.unit_liters_short,
            decimalFormat.format(litersMilliliters / 1000.0),
        ),
        pricePerLiterLabel = stringResourceProvider.get(
            R.string.unit_price_per_unit_eur,
            decimalFormat.format(pricePerLiterMicros / 1_000_000.0),
        ),
    )
}

private val decimalFormat = DecimalFormat("0.0#")

private fun Long.toEuroString(): String = "${decimalFormat.format(this / 100.0)} €"

private fun Long.toLitersString(): String = "${decimalFormat.format(this / 1000.0)} L"

private fun Trip.toUiModel(
    stringResourceProvider: StringResourceProvider,
    nowEpochMillis: Long,
): DashboardActiveTripUiModel = DashboardActiveTripUiModel(
    id = id,
    title = title ?: stringResourceProvider.get(R.string.dashboard_active_trip_default_title),
    locationLabel = startLocation ?: stringResourceProvider.get(R.string.dashboard_no_start_location),
    elapsedLabel = stringResourceProvider.get(
        R.string.dashboard_active_trip_elapsed,
        formatTripElapsed((nowEpochMillis - startTimeEpochMillis).coerceAtLeast(0L), stringResourceProvider),
    ),
)

private fun formatTripElapsed(
    durationMillis: Long,
    stringResourceProvider: StringResourceProvider,
): String {
    val duration = Duration.ofMillis(durationMillis)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    return if (hours > 0) {
        stringResourceProvider.get(R.string.trip_summary_duration_hours_minutes, hours, minutes)
    } else {
        stringResourceProvider.get(R.string.trip_summary_duration_minutes, minutes)
    }
}

private val minuteTicker = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(60_000L)
    }
}
