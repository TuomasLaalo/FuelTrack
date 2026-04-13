package com.laatu.fueltank.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import com.laatu.fueltank.domain.usecase.RenameTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
) : ViewModel() {
    val uiState: StateFlow<TripsUiState> = observePrimaryCarUseCase()
        .flatMapLatest { car ->
            if (car == null) {
                flowOf(TripsUiState())
            } else {
                tripRepository.observeTripsForCar(car.id).map { trips ->
                    TripsUiState(
                        totalTripsLabel = trips.size.toString(),
                        activeTripsLabel = trips.count { it.status == TripStatus.ACTIVE }.toString(),
                        completedTripsLabel = trips.count { it.status == TripStatus.COMPLETED }.toString(),
                        items = trips.map { it.toUiItem() },
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

private fun Trip.toUiItem(): TripListItem {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault())
    val statusLabel = if (status == TripStatus.ACTIVE) "Active" else "Completed"
    val distanceLabel = endOdometerKm?.let { "${it - startOdometerKm} km" } ?: "In progress"
    return TripListItem(
        id = id,
        isActive = status == TripStatus.ACTIVE,
        statusLabel = statusLabel,
        title = title ?: startLocation ?: "Trip",
        dateLabel = formatter.format(Instant.ofEpochMilli(startTimeEpochMillis)),
        startLocationLabel = startLocation ?: "No start location",
        distanceLabel = distanceLabel,
    )
}
