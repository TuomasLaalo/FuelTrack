package com.laatu.fueltank.domain.model

data class FuelStats(
    val totalCostCents: Long,
    val totalLitersMilliliters: Long,
    val reliableDistanceKm: Long,
    val reliableLitersUsedMilliliters: Long,
    val reliableCostCents: Long,
    val averageConsumptionLitersPer100Km: Double?,
    val reliableCostPerKilometerCents: Double?,
    val confidence: CalculationConfidence,
)
