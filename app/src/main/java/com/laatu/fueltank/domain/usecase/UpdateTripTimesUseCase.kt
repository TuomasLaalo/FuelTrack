package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.TripBreakMutationError
import com.laatu.fueltank.domain.service.TripBreakMutationException
import javax.inject.Inject

class UpdateTripTimesUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripBreakRepository: TripBreakRepository,
) {
    suspend operator fun invoke(
        tripId: Long,
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long?,
    ) {
        val trip = tripRepository.getTrip(tripId)
            ?: throw TripBreakMutationException(TripBreakMutationError.TRIP_NOT_FOUND)
        val resolvedEndTimeEpochMillis = when (trip.status) {
            TripStatus.ACTIVE -> null
            TripStatus.COMPLETED -> endTimeEpochMillis
                ?: throw TripBreakMutationException(TripBreakMutationError.NEGATIVE_DURATION)
        }
        if (resolvedEndTimeEpochMillis != null && resolvedEndTimeEpochMillis < startTimeEpochMillis) {
            throw TripBreakMutationException(TripBreakMutationError.NEGATIVE_DURATION)
        }
        val breaks = tripBreakRepository.getBreaksForTrip(tripId)
        val outsideRange = breaks.any { tripBreak ->
            val breakEnd = tripBreak.endTimeEpochMillis ?: resolvedEndTimeEpochMillis
            tripBreak.startTimeEpochMillis < startTimeEpochMillis ||
                (resolvedEndTimeEpochMillis != null && breakEnd != null && breakEnd > resolvedEndTimeEpochMillis)
        }
        if (outsideRange) {
            throw TripBreakMutationException(TripBreakMutationError.BREAK_OUTSIDE_TRIP)
        }
        val now = System.currentTimeMillis()
        tripRepository.upsertTrip(
            trip.copy(
                startTimeEpochMillis = startTimeEpochMillis,
                endTimeEpochMillis = resolvedEndTimeEpochMillis,
                updatedAtEpochMillis = now,
            ),
        )
    }
}
