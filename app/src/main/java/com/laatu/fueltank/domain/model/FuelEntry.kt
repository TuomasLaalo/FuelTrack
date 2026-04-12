package com.laatu.fueltank.domain.model

data class FuelEntry(
    val id: Long,
    val carId: Long,
    val tripId: Long?,
    val dateTimeEpochMillis: Long,
    val distanceInputMode: DistanceInputMode,
    val odometerKm: Long,
    val tripMeterKm: Long?,
    val litersMilliliters: Long,
    val totalCostCents: Long,
    val pricePerLiterMicros: Long,
    val stationBrand: String? = null,
    val isFullTank: Boolean,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
