package com.laatu.fueltank.domain.model

data class TripTimelineItem(
    val id: String,
    val type: TripTimelineItemType,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long? = null,
    val durationMillis: Long = 0L,
    val note: String? = null,
    val fuelLitersMilliliters: Long? = null,
    val fuelCostCents: Long? = null,
)

enum class TripTimelineItemType {
    TRIP_STARTED,
    BREAK,
    FUEL_ENTRY,
    TRIP_ENDED,
}
