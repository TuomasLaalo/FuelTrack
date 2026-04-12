package com.laatu.fueltank.domain.model

data class Trip(
    val id: Long,
    val carId: Long,
    val title: String?,
    val startLocation: String?,
    val endLocation: String?,
    val startOdometerKm: Long,
    val endOdometerKm: Long?,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val status: TripStatus,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
