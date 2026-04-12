package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.ConsumptionSegment
import com.laatu.fueltank.domain.model.FuelEntry
import javax.inject.Inject

class ConsumptionSegmentBuilder @Inject constructor() {
    fun build(entries: List<FuelEntry>): List<ConsumptionSegment> {
        val ordered = entries.sortedWith(compareBy<FuelEntry> { it.dateTimeEpochMillis }.thenBy { it.odometerKm })
        val fullTankAnchors = ordered.filter { it.isFullTank }
        if (fullTankAnchors.size < 2) return emptyList()

        val segments = mutableListOf<ConsumptionSegment>()
        var startAnchor: FuelEntry? = null

        ordered.forEach { entry ->
            if (entry.isFullTank) {
                if (startAnchor == null) {
                    startAnchor = entry
                } else {
                    val segmentEntries = ordered.filter {
                        it.dateTimeEpochMillis > startAnchor!!.dateTimeEpochMillis &&
                            it.dateTimeEpochMillis <= entry.dateTimeEpochMillis
                    }
                    val distanceKm = entry.odometerKm - startAnchor!!.odometerKm
                    val fuelUsedMilliliters = segmentEntries.sumOf { it.litersMilliliters }
                    val costCents = segmentEntries.sumOf { it.totalCostCents }

                    if (distanceKm > 0 && fuelUsedMilliliters > 0) {
                        segments += ConsumptionSegment(
                            startEntryId = startAnchor!!.id,
                            endEntryId = entry.id,
                            distanceKm = distanceKm,
                            fuelUsedMilliliters = fuelUsedMilliliters,
                            costCents = costCents,
                            confidence = CalculationConfidence.HIGH_CONFIDENCE,
                        )
                    }
                    startAnchor = entry
                }
            }
        }

        return segments
    }
}
