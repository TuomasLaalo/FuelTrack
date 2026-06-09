package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripBreak
import javax.inject.Inject

class TripBreakValidationService @Inject constructor() {
    fun validate(
        trip: Trip,
        candidate: TripBreak,
        existingBreaks: List<TripBreak>,
    ) {
        if (candidate.tripId != trip.id) {
            throw TripBreakMutationException(TripBreakMutationError.TRIP_NOT_FOUND)
        }
        if (candidate.startTimeEpochMillis < trip.startTimeEpochMillis) {
            throw TripBreakMutationException(TripBreakMutationError.BREAK_OUTSIDE_TRIP)
        }
        val candidateEnd = candidate.endTimeEpochMillis
        if (candidateEnd != null && candidateEnd < candidate.startTimeEpochMillis) {
            throw TripBreakMutationException(TripBreakMutationError.NEGATIVE_DURATION)
        }
        val tripEnd = trip.endTimeEpochMillis
        if (tripEnd != null && candidateEnd != null && candidateEnd > tripEnd) {
            throw TripBreakMutationException(TripBreakMutationError.BREAK_OUTSIDE_TRIP)
        }

        val overlaps = existingBreaks
            .filterNot { it.id == candidate.id }
            .any { existing -> candidate.overlaps(existing) }
        if (overlaps) {
            throw TripBreakMutationException(TripBreakMutationError.OVERLAPPING_BREAKS)
        }
    }

    private fun TripBreak.overlaps(other: TripBreak): Boolean {
        val thisEnd = endTimeEpochMillis ?: Long.MAX_VALUE
        val otherEnd = other.endTimeEpochMillis ?: Long.MAX_VALUE
        return startTimeEpochMillis < otherEnd && other.startTimeEpochMillis < thisEnd
    }
}

enum class TripBreakMutationError {
    TRIP_NOT_FOUND,
    BREAK_NOT_FOUND,
    ACTIVE_BREAK_EXISTS,
    NEGATIVE_DURATION,
    OVERLAPPING_BREAKS,
    BREAK_OUTSIDE_TRIP,
}

class TripBreakMutationException(
    val error: TripBreakMutationError,
) : IllegalStateException(error.name)
