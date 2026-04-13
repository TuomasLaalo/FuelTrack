package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.TripRepository
import javax.inject.Inject

class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: Long) {
        tripRepository.deleteTrip(tripId)
    }
}
