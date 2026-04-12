package com.laatu.fueltank.domain.model

data class TripSummary(
    val tripId: Long,
    val distanceKm: Long,
    val fuelBoughtMilliliters: Long,
    val fuelCostCents: Long,
    val averageConsumptionLitersPer100Km: Double?,
    val confidence: CalculationConfidence,
)
