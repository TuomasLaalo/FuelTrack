package com.laatu.fueltank.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

        return DashboardUiState(
            carName = car.name,
            currentOdometerLabel = "${currentOdometer.odometerKm} km",
            totalFuelCostLabel = stats.totalCostCents.toEuroString(),
            totalFuelBoughtLabel = stats.totalLitersMilliliters.toLitersString(),
            totalDistanceLabel = stats.reliableDistanceKm.takeIf { it > 0 }?.let { "$it km" } ?: "No reliable distance",
            heroConsumptionValue = stats.averageConsumptionLitersPer100Km?.let { decimalFormat.format(it) } ?: "--",
            heroConsumptionUnit = if (stats.averageConsumptionLitersPer100Km != null) "L/100 km" else "Waiting for reliable fills",
            heroTrendMessage = when (stats.confidence) {
                CalculationConfidence.HIGH_CONFIDENCE -> "Reliable full-to-full calculation"
                CalculationConfidence.LOW_CONFIDENCE -> "Low confidence estimate"
                CalculationConfidence.NOT_AVAILABLE -> "Add two full fills to unlock reliable consumption"
            },
            activeTrip = activeTrip?.toUiModel(),
            latestRefuel = latestEntry?.toLatestRefuelCard(),
            utilityCard = DashboardUtilityUiModel(
                title = "Fuel history",
                subtitle = "Review previous refuels and compare costs over time.",
                actionLabel = "Open history",
            ),
        )
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val carName: String = "",
    val currentOdometerLabel: String = "Not available",
    val totalFuelCostLabel: String = "0.00 EUR",
    val totalFuelBoughtLabel: String = "0.00 L",
    val totalDistanceLabel: String = "No reliable distance",
    val heroConsumptionValue: String = "--",
    val heroConsumptionUnit: String = "Waiting for reliable fills",
    val heroTrendMessage: String = "Add two full fills to unlock reliable consumption",
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

private fun FuelEntry.toLatestRefuelCard(): DashboardLatestRefuelUiModel {
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(ZoneId.systemDefault())
    return DashboardLatestRefuelUiModel(
        dateLabel = formatter.format(Instant.ofEpochMilli(dateTimeEpochMillis)),
        totalCostLabel = totalCostCents.toEuroString(),
        litersLabel = "${decimalFormat.format(litersMilliliters / 1000.0)} L",
        pricePerLiterLabel = "${decimalFormat.format(pricePerLiterMicros / 1_000_000.0)} EUR/L",
    )
}

private val decimalFormat = DecimalFormat("0.0#")

private fun Long.toEuroString(): String = "${decimalFormat.format(this / 100.0)} EUR"

private fun Long.toLitersString(): String = "${decimalFormat.format(this / 1000.0)} L"

private fun Trip.toUiModel(): DashboardActiveTripUiModel = DashboardActiveTripUiModel(
    id = id,
    title = title ?: "Active trip",
    subtitle = buildString {
        append(startLocation ?: "No start location")
        append(" • ")
        append("$startOdometerKm km")
    },
)
