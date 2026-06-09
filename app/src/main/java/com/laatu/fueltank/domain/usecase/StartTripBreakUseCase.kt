package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.TripBreakMutationError
import com.laatu.fueltank.domain.service.TripBreakMutationException
import com.laatu.fueltank.domain.service.TripBreakValidationService
import javax.inject.Inject

class StartTripBreakUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val tripRepository: TripRepository,
    private val tripBreakRepository: TripBreakRepository,
    private val tripBreakValidationService: TripBreakValidationService,
) {
    suspend operator fun invoke(): Long {
        val car = carRepository.getPrimaryCar() ?: return 0L
        val activeTrip = tripRepository.getActiveTripForCar(car.id) ?: return 0L
        if (tripBreakRepository.getActiveBreakForTrip(activeTrip.id) != null) {
            throw TripBreakMutationException(TripBreakMutationError.ACTIVE_BREAK_EXISTS)
        }
        val now = System.currentTimeMillis()
        val tripBreak = TripBreak(
            id = 0,
            tripId = activeTrip.id,
            startTimeEpochMillis = now,
            endTimeEpochMillis = null,
            note = null,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        tripBreakValidationService.validate(
            trip = activeTrip,
            candidate = tripBreak,
            existingBreaks = tripBreakRepository.getBreaksForTrip(activeTrip.id),
        )
        return tripBreakRepository.upsertBreak(tripBreak)
    }
}
