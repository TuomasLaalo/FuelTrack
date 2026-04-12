package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.TripRepository
import javax.inject.Inject

class RenameTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        tripId: Long,
        title: String,
    ) {
        val trip = tripRepository.getTrip(tripId) ?: return
        tripRepository.upsertTrip(
            trip.copy(
                title = title.trim().ifBlank { null },
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }
}
