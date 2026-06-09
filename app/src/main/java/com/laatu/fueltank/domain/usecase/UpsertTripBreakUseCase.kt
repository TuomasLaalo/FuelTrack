package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.TripBreakMutationError
import com.laatu.fueltank.domain.service.TripBreakMutationException
import com.laatu.fueltank.domain.service.TripBreakValidationService
import javax.inject.Inject

class UpsertTripBreakUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripBreakRepository: TripBreakRepository,
    private val tripBreakValidationService: TripBreakValidationService,
) {
    suspend operator fun invoke(
        tripId: Long,
        breakId: Long?,
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long?,
        note: String?,
    ): Long {
        val trip = tripRepository.getTrip(tripId)
            ?: throw TripBreakMutationException(TripBreakMutationError.TRIP_NOT_FOUND)
        val now = System.currentTimeMillis()
        val existing = breakId?.let {
            tripBreakRepository.getBreak(it)
                ?: throw TripBreakMutationException(TripBreakMutationError.BREAK_NOT_FOUND)
        }
        val tripBreak = TripBreak(
            id = existing?.id ?: 0L,
            tripId = tripId,
            startTimeEpochMillis = startTimeEpochMillis,
            endTimeEpochMillis = endTimeEpochMillis,
            note = note?.trim().orEmpty().ifBlank { null },
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
        )
        tripBreakValidationService.validate(
            trip = trip,
            candidate = tripBreak,
            existingBreaks = tripBreakRepository.getBreaksForTrip(tripId),
        )
        return tripBreakRepository.upsertBreak(tripBreak)
    }
}
