package com.laatu.fueltank.domain.model

data class TripSummary(
    val tripId: Long,
    val distanceKm: Long,
    val fuelBoughtMilliliters: Long,
    val fuelCostCents: Long,
    val averageConsumptionLitersPer100Km: Double?,
    val purchaseBasedFuelLitersPer100Km: Double?,
    val totalElapsedTimeMillis: Long,
    val totalBreakTimeMillis: Long,
    val drivingTimeMillis: Long,
    val averageSpeedIncludingBreaksKmH: Double?,
    val averageSpeedExcludingBreaksKmH: Double?,
    val timeline: List<TripTimelineItem>,
    val confidence: CalculationConfidence,
)
