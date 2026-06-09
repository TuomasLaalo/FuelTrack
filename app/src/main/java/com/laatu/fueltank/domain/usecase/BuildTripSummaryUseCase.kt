package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.TripSummary
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripBreakRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.TripSummaryService
import javax.inject.Inject

class BuildTripSummaryUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val fuelEntryRepository: FuelEntryRepository,
    private val tripBreakRepository: TripBreakRepository,
    private val tripSummaryService: TripSummaryService,
) {
    suspend operator fun invoke(tripId: Long): TripSummary? {
        val trip = tripRepository.getTrip(tripId) ?: return null
        val linkedEntries = fuelEntryRepository.getEntriesForCar(trip.carId)
            .filter { it.tripId == tripId }
        val tripBreaks = tripBreakRepository.getBreaksForTrip(tripId)
        return tripSummaryService.buildSummary(
            trip = trip,
            linkedEntries = linkedEntries,
            tripBreaks = tripBreaks,
        )
    }
}
