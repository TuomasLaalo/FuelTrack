package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripSummary
import javax.inject.Inject

class TripSummaryService @Inject constructor() {
    fun buildSummary(
        trip: Trip,
        linkedEntries: List<FuelEntry>,
    ): TripSummary {
        val endOdometerKm = trip.endOdometerKm
        val distanceKm = if (endOdometerKm != null && endOdometerKm >= trip.startOdometerKm) {
            endOdometerKm - trip.startOdometerKm
        } else {
            0L
        }

        val fuelBoughtMilliliters = linkedEntries.sumOf { it.litersMilliliters }
        val fuelCostCents = linkedEntries.sumOf { it.totalCostCents }
        // Trip consumption is intentionally conservative in v1: it is based on refuels
        // explicitly linked to the trip, not inferred tank-state estimation.
        val averageConsumption = if (distanceKm > 0 && fuelBoughtMilliliters > 0) {
            (fuelBoughtMilliliters / 1000.0) / distanceKm * 100.0
        } else {
            null
        }

        val confidence = if (averageConsumption != null) {
            CalculationConfidence.LOW_CONFIDENCE
        } else {
            CalculationConfidence.NOT_AVAILABLE
        }

        return TripSummary(
            tripId = trip.id,
            distanceKm = distanceKm,
            fuelBoughtMilliliters = fuelBoughtMilliliters,
            fuelCostCents = fuelCostCents,
            averageConsumptionLitersPer100Km = averageConsumption,
            confidence = confidence,
        )
    }
}
