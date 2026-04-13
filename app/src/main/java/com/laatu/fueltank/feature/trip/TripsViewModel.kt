package com.laatu.fueltank.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.usecase.DeleteTripUseCase
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.RenameTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TripsViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    tripRepository: TripRepository,
    private val renameTripUseCase: RenameTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    val uiState: StateFlow<TripsUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                flowOf(TripsUiState())
            } else {
                tripRepository.observeTripsForCar(car.id).map { trips ->
                    val locale = stringResourceProvider.currentLocale()
                    TripsUiState(
                        totalTripsLabel = trips.size.toString(),
                        activeTripsLabel = trips.count { it.status == TripStatus.ACTIVE }.toString(),
                        completedTripsLabel = trips.count { it.status == TripStatus.COMPLETED }.toString(),
                        items = trips.map { it.toUiItem(stringResourceProvider, locale) },
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripsUiState(),
        )

    fun renameTrip(
        tripId: Long,
        title: String,
    ) {
        viewModelScope.launch {
            renameTripUseCase(
                tripId = tripId,
                title = title,
            )
        }
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            deleteTripUseCase(tripId)
        }
    }
}

data class TripsUiState(
    val isLoading: Boolean = false,
    val totalTripsLabel: String = "0",
    val activeTripsLabel: String = "0",
    val completedTripsLabel: String = "0",
    val items: List<TripListItem> = emptyList(),
)

data class TripListItem(
    val id: Long,
    val isActive: Boolean,
    val statusLabel: String,
    val title: String,
    val dateLabel: String,
    val startLocationLabel: String,
    val distanceLabel: String,
)

private fun Trip.toUiItem(
    stringResourceProvider: StringResourceProvider,
    locale: Locale,
): TripListItem {
    val formatter = DateTimeFormatter
        .ofPattern(stringResourceProvider.get(R.string.trip_history_date_format), locale)
        .withZone(ZoneId.systemDefault())
    val statusLabel = if (status == TripStatus.ACTIVE) {
        stringResourceProvider.get(R.string.trip_history_status_active)
    } else {
        stringResourceProvider.get(R.string.trip_history_status_completed)
    }
    val distanceLabel = endOdometerKm?.let {
        stringResourceProvider.get(R.string.dashboard_distance_km, it - startOdometerKm)
    } ?: stringResourceProvider.get(R.string.trip_history_in_progress)
    return TripListItem(
        id = id,
        isActive = status == TripStatus.ACTIVE,
        statusLabel = statusLabel,
        title = title ?: startLocation ?: stringResourceProvider.get(R.string.trip_history_default_title),
        dateLabel = formatter.format(Instant.ofEpochMilli(startTimeEpochMillis)),
        startLocationLabel = startLocation ?: stringResourceProvider.get(R.string.trip_history_no_start_location),
        distanceLabel = distanceLabel,
    )
}
