package com.laatu.fueltank.domain.model

data class TripBreak(
    val id: Long,
    val tripId: Long,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
