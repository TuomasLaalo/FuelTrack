package com.laatu.fueltank.domain.model

data class ConsumptionSegment(
    val startEntryId: Long,
    val endEntryId: Long,
    val distanceKm: Long,
    val fuelUsedMilliliters: Long,
    val costCents: Long,
    val confidence: CalculationConfidence,
)
