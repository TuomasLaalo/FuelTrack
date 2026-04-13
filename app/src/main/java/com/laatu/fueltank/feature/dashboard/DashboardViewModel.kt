package com.laatu.fueltank.feature.dashboard

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
import com.laatu.fueltank.domain.usecase.BuildDashboardStatsUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
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

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    tripRepository: TripRepository,
    private val currentOdometerService: CurrentOdometerService,
    private val buildDashboardStatsUseCase: BuildDashboardStatsUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                flowOf(DashboardUiState())
            } else {
                combine(
                    flowOf(car),
                    fuelEntryRepository.observeEntriesForCar(car.id),
                    tripRepository.observeTripsForCar(car.id),
                ) { activeCar, entries, trips ->
                    toUiState(activeCar, entries, trips)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(isLoading = true),
        )

    private fun toUiState(car: Car, entries: List<FuelEntry>, trips: List<Trip>): DashboardUiState {
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
            activeTrip = activeTrip?.toUiModel(stringResourceProvider),
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
    val activeTrip: DashboardActiveTripUiModel? = null,
    val latestRefuel: DashboardLatestRefuelUiModel? = null,
    val utilityCard: DashboardUtilityUiModel? = null,
)

data class DashboardActiveTripUiModel(
    val id: Long,
    val title: String,
    val subtitle: String,
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
): DashboardActiveTripUiModel = DashboardActiveTripUiModel(
    id = id,
    title = title ?: stringResourceProvider.get(R.string.dashboard_active_trip_default_title),
    subtitle = stringResourceProvider.get(
        R.string.dashboard_active_trip_subtitle,
        startLocation ?: stringResourceProvider.get(R.string.dashboard_no_start_location),
        startOdometerKm,
    ),
)
