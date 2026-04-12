package com.laatu.fueltank.feature.fuelhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.service.FuelEntryMutationException
import com.laatu.fueltank.domain.usecase.BuildDashboardStatsUseCase
import com.laatu.fueltank.domain.usecase.DeleteFuelEntryUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
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
class FuelHistoryViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    fuelEntryRepository: FuelEntryRepository,
    private val buildDashboardStatsUseCase: BuildDashboardStatsUseCase,
    private val deleteFuelEntryUseCase: DeleteFuelEntryUseCase,
) : ViewModel() {
    private val pendingDeleteId = MutableStateFlow<Long?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val sortOrder = MutableStateFlow(FuelHistorySortOrder.NEWEST_FIRST)

    val uiState: StateFlow<FuelHistoryUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                combine(
                    pendingDeleteId,
                    errorMessage,
                    sortOrder,
                    flowOf(emptyList<FuelEntry>()),
                ) { pendingId, message, order, entries ->
                    entries.toUiState(
                        pendingDeleteId = pendingId,
                        errorMessage = message,
                        sortOrder = order,
                        buildDashboardStatsUseCase = buildDashboardStatsUseCase,
                    )
                }
            } else {
                combine(
                    pendingDeleteId,
                    errorMessage,
                    sortOrder,
                    fuelEntryRepository.observeEntriesForCar(car.id),
                ) { pendingId, message, order, entries ->
                    entries.toUiState(
                        pendingDeleteId = pendingId,
                        errorMessage = message,
                        sortOrder = order,
                        buildDashboardStatsUseCase = buildDashboardStatsUseCase,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FuelHistoryUiState(isLoading = true),
        )

    fun toggleSortOrder() {
        sortOrder.value = when (sortOrder.value) {
            FuelHistorySortOrder.NEWEST_FIRST -> FuelHistorySortOrder.OLDEST_FIRST
            FuelHistorySortOrder.OLDEST_FIRST -> FuelHistorySortOrder.NEWEST_FIRST
        }
    }

    fun requestDelete(entryId: Long) {
        errorMessage.value = null
        pendingDeleteId.value = entryId
    }

    fun dismissDeleteDialog() {
        pendingDeleteId.value = null
    }

    fun dismissError() {
        errorMessage.value = null
    }

    fun confirmDelete() {
        val entryId = pendingDeleteId.value ?: return
        viewModelScope.launch {
            try {
                deleteFuelEntryUseCase(entryId)
            } catch (error: FuelEntryMutationException) {
                errorMessage.value = error.message
            }
            pendingDeleteId.value = null
        }
    }
}

data class FuelHistoryUiState(
    val isLoading: Boolean = false,
    val summary: FuelHistorySummaryUiModel? = null,
    val sections: List<FuelHistorySectionUiModel> = emptyList(),
    val pendingDeleteId: Long? = null,
    val errorMessage: String? = null,
    val sortOrder: FuelHistorySortOrder = FuelHistorySortOrder.NEWEST_FIRST,
)

data class FuelHistorySummaryUiModel(
    val totalRefuelsLabel: String,
    val totalQuantityLabel: String,
    val averageConsumptionLabel: String,
)

data class FuelHistorySectionUiModel(
    val title: String,
    val entries: List<FuelHistoryItem>,
)

data class FuelHistoryItem(
    val id: Long,
    val stationBrand: String?,
    val dateLabel: String,
    val totalCostLabel: String,
    val quantityLabel: String,
    val pricePerUnitLabel: String,
    val note: String?,
)

enum class FuelHistorySortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
}

private fun List<FuelEntry>.toUiState(
    pendingDeleteId: Long?,
    errorMessage: String?,
    sortOrder: FuelHistorySortOrder,
    buildDashboardStatsUseCase: BuildDashboardStatsUseCase,
): FuelHistoryUiState {
    val orderedEntries = when (sortOrder) {
        FuelHistorySortOrder.NEWEST_FIRST -> sortedByDescending { it.dateTimeEpochMillis }
        FuelHistorySortOrder.OLDEST_FIRST -> sortedBy { it.dateTimeEpochMillis }
    }
    val sections = orderedEntries
        .groupBy { monthFormatter.format(Instant.ofEpochMilli(it.dateTimeEpochMillis)).uppercase(Locale.getDefault()) }
        .map { (title, entries) ->
            FuelHistorySectionUiModel(
                title = title,
                entries = entries.map { it.toUiItem() },
            )
        }

    val stats = buildDashboardStatsUseCase(this)
    val summary = FuelHistorySummaryUiModel(
        totalRefuelsLabel = size.toString(),
        totalQuantityLabel = litersFormat.format(stats.totalLitersMilliliters / 1000.0) + " L",
        averageConsumptionLabel = when (stats.confidence) {
            CalculationConfidence.HIGH_CONFIDENCE -> {
                stats.averageConsumptionLitersPer100Km?.let { consumptionFormat.format(it) + " L/100km" } ?: "Not yet"
            }
            else -> "Not yet"
        },
    )

    return FuelHistoryUiState(
        isLoading = false,
        summary = summary,
        sections = sections,
        pendingDeleteId = pendingDeleteId,
        errorMessage = errorMessage,
        sortOrder = sortOrder,
    )
}

private fun FuelEntry.toUiItem(): FuelHistoryItem = FuelHistoryItem(
    id = id,
    stationBrand = stationBrand,
    dateLabel = dateFormatter.format(Instant.ofEpochMilli(dateTimeEpochMillis)),
    totalCostLabel = totalCostCents.toEuroString(),
    quantityLabel = litersFormat.format(litersMilliliters / 1000.0) + " L",
    pricePerUnitLabel = pricePerLiterMicros.toEuroPerUnitString(),
    note = note?.trim()?.takeIf { it.isNotEmpty() },
)

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault())
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy").withZone(ZoneId.systemDefault())
private val litersFormat = DecimalFormat("0.0#")
private val consumptionFormat = DecimalFormat("0.0")
private val euroFormat = DecimalFormat("0.00")

private fun Long.toEuroString(): String = "€" + euroFormat.format(this / 100.0)

private fun Long.toEuroPerUnitString(): String = "€" + euroFormat.format(this / 1_000_000.0)
