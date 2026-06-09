package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.model.TripSummary
import com.laatu.fueltank.domain.model.TripTimelineItem
import com.laatu.fueltank.domain.model.TripTimelineItemType
import javax.inject.Inject

class TripSummaryService @Inject constructor() {
    fun buildSummary(
        trip: Trip,
        linkedEntries: List<FuelEntry>,
        tripBreaks: List<TripBreak> = emptyList(),
        currentTimeEpochMillis: Long = System.currentTimeMillis(),
    ): TripSummary {
        val endOdometerKm = trip.endOdometerKm
        val distanceKm = if (endOdometerKm != null && endOdometerKm >= trip.startOdometerKm) {
            endOdometerKm - trip.startOdometerKm
        } else {
            0L
        }
        val effectiveEndTime = trip.endTimeEpochMillis ?: currentTimeEpochMillis
        val totalElapsedTimeMillis = (effectiveEndTime - trip.startTimeEpochMillis).coerceAtLeast(0L)
        val totalBreakTimeMillis = tripBreaks.sumOf { tripBreak ->
            val breakEnd = tripBreak.endTimeEpochMillis ?: currentTimeEpochMillis
            (breakEnd - tripBreak.startTimeEpochMillis).coerceAtLeast(0L)
        }.coerceAtMost(totalElapsedTimeMillis)
        val drivingTimeMillis = (totalElapsedTimeMillis - totalBreakTimeMillis).coerceAtLeast(0L)

        val fuelBoughtMilliliters = linkedEntries.sumOf { it.litersMilliliters }
        val fuelCostCents = linkedEntries.sumOf { it.totalCostCents }
        val purchaseBasedFuelPer100Km = if (distanceKm > 0 && fuelBoughtMilliliters > 0) {
            (fuelBoughtMilliliters / 1000.0) / distanceKm * 100.0
        } else {
            null
        }
        val averageSpeedIncludingBreaks = calculateAverageSpeed(
            distanceKm = distanceKm,
            durationMillis = totalElapsedTimeMillis,
        )
        val averageSpeedExcludingBreaks = calculateAverageSpeed(
            distanceKm = distanceKm,
            durationMillis = drivingTimeMillis,
        )

        return TripSummary(
            tripId = trip.id,
            distanceKm = distanceKm,
            fuelBoughtMilliliters = fuelBoughtMilliliters,
            fuelCostCents = fuelCostCents,
            averageConsumptionLitersPer100Km = null,
            purchaseBasedFuelLitersPer100Km = purchaseBasedFuelPer100Km,
            totalElapsedTimeMillis = totalElapsedTimeMillis,
            totalBreakTimeMillis = totalBreakTimeMillis,
            drivingTimeMillis = drivingTimeMillis,
            averageSpeedIncludingBreaksKmH = averageSpeedIncludingBreaks,
            averageSpeedExcludingBreaksKmH = averageSpeedExcludingBreaks,
            timeline = buildTimeline(
                trip = trip,
                linkedEntries = linkedEntries,
                tripBreaks = tripBreaks,
                currentTimeEpochMillis = currentTimeEpochMillis,
            ),
            confidence = CalculationConfidence.NOT_AVAILABLE,
        )
    }

    private fun calculateAverageSpeed(
        distanceKm: Long,
        durationMillis: Long,
    ): Double? {
        if (distanceKm <= 0L || durationMillis <= 0L) return null
        val speed = distanceKm / (durationMillis / MILLIS_PER_HOUR)
        return speed.takeIf { it <= MAX_REASONABLE_TRIP_SPEED_KMH }
    }

    private fun buildTimeline(
        trip: Trip,
        linkedEntries: List<FuelEntry>,
        tripBreaks: List<TripBreak>,
        currentTimeEpochMillis: Long,
    ): List<TripTimelineItem> {
        val items = buildList {
            add(
                TripTimelineItem(
                    id = "trip-start-${trip.id}",
                    type = TripTimelineItemType.TRIP_STARTED,
                    startTimeEpochMillis = trip.startTimeEpochMillis,
                ),
            )
            tripBreaks.forEach { tripBreak ->
                val endTime = tripBreak.endTimeEpochMillis ?: currentTimeEpochMillis
                add(
                    TripTimelineItem(
                        id = "break-${tripBreak.id}",
                        type = TripTimelineItemType.BREAK,
                        startTimeEpochMillis = tripBreak.startTimeEpochMillis,
                        endTimeEpochMillis = tripBreak.endTimeEpochMillis,
                        durationMillis = (endTime - tripBreak.startTimeEpochMillis).coerceAtLeast(0L),
                        note = tripBreak.note,
                    ),
                )
            }
            linkedEntries.forEach { entry ->
                add(
                    TripTimelineItem(
                        id = "fuel-${entry.id}",
                        type = TripTimelineItemType.FUEL_ENTRY,
                        startTimeEpochMillis = entry.dateTimeEpochMillis,
                        fuelLitersMilliliters = entry.litersMilliliters,
                        fuelCostCents = entry.totalCostCents,
                        note = entry.stationBrand ?: entry.note,
                    ),
                )
            }
            trip.endTimeEpochMillis?.let { endTime ->
                add(
                    TripTimelineItem(
                        id = "trip-end-${trip.id}",
                        type = TripTimelineItemType.TRIP_ENDED,
                        startTimeEpochMillis = endTime,
                    ),
                )
            }
        }

        return items.sortedWith(
            compareBy<TripTimelineItem> { it.startTimeEpochMillis }.thenBy { it.type.ordinal }.thenBy { it.id },
        )
    }

    private companion object {
        const val MILLIS_PER_HOUR = 3_600_000.0
        const val MAX_REASONABLE_TRIP_SPEED_KMH = 250.0
    }
}
