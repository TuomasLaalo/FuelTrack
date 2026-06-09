package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.TripBreakValidationService
import javax.inject.Inject

class EndCurrentTripBreakUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val tripRepository: TripRepository,
    private val tripBreakRepository: TripBreakRepository,
    private val tripBreakValidationService: TripBreakValidationService,
) {
    suspend operator fun invoke(): Long {
        val car = carRepository.getPrimaryCar() ?: return 0L
        val activeTrip = tripRepository.getActiveTripForCar(car.id) ?: return 0L
        val activeBreak = tripBreakRepository.getActiveBreakForTrip(activeTrip.id) ?: return 0L
        val now = System.currentTimeMillis()
        val endedBreak = activeBreak.copy(
            endTimeEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        tripBreakValidationService.validate(
            trip = activeTrip,
            candidate = endedBreak,
            existingBreaks = tripBreakRepository.getBreaksForTrip(activeTrip.id),
        )
        return tripBreakRepository.upsertBreak(endedBreak)
    }
}
