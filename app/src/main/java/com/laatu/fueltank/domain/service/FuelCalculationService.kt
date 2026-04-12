package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.FuelStats
import javax.inject.Inject

class FuelCalculationService @Inject constructor(
    private val consumptionSegmentBuilder: ConsumptionSegmentBuilder,
) {
    fun calculateStats(entries: List<FuelEntry>): FuelStats {
        val segments = consumptionSegmentBuilder.build(entries)
        val totalCostCents = entries.sumOf { it.totalCostCents }
        val totalLitersMilliliters = entries.sumOf { it.litersMilliliters }
        val reliableDistanceKm = segments.sumOf { it.distanceKm }
        val reliableLitersUsedMilliliters = segments.sumOf { it.fuelUsedMilliliters }
        val reliableCostCents = segments.sumOf { it.costCents }

        val averageConsumption = if (reliableDistanceKm > 0 && reliableLitersUsedMilliliters > 0) {
            (reliableLitersUsedMilliliters / 1000.0) / reliableDistanceKm * 100.0
        } else {
            null
        }

        val reliableCostPerKilometerCents = if (reliableDistanceKm > 0 && reliableCostCents > 0) {
            reliableCostCents.toDouble() / reliableDistanceKm
        } else {
            null
        }

        return FuelStats(
            totalCostCents = totalCostCents,
            totalLitersMilliliters = totalLitersMilliliters,
            reliableDistanceKm = reliableDistanceKm,
            reliableLitersUsedMilliliters = reliableLitersUsedMilliliters,
            reliableCostCents = reliableCostCents,
            averageConsumptionLitersPer100Km = averageConsumption,
            reliableCostPerKilometerCents = reliableCostPerKilometerCents,
            confidence = if (segments.isEmpty()) {
                CalculationConfidence.NOT_AVAILABLE
            } else {
                CalculationConfidence.HIGH_CONFIDENCE
            },
        )
    }
}
