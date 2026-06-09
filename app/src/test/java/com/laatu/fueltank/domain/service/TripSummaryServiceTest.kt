package com.laatu.fueltank.domain.service

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.model.TripTimelineItemType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TripSummaryServiceTest {
    private val service = TripSummaryService()

    @Test
    fun buildSummary_returnsDistanceAndTripLinkedPurchaseTotals() {
        val summary = service.buildSummary(
            trip = trip(endOdometerKm = 10_450),
            linkedEntries = listOf(
                entry(id = 1, tripId = 7, litersMilliliters = 20_000, totalCostCents = 3_500),
                entry(id = 2, tripId = 7, litersMilliliters = 15_000, totalCostCents = 2_700),
            ),
        )

        assertThat(summary.distanceKm).isEqualTo(450)
        assertThat(summary.fuelBoughtMilliliters).isEqualTo(35_000)
        assertThat(summary.fuelCostCents).isEqualTo(6_200)
        assertThat(summary.averageConsumptionLitersPer100Km).isNull()
        assertThat(summary.purchaseBasedFuelLitersPer100Km).isWithin(0.0001).of(7.777777777777778)
        assertThat(summary.confidence).isEqualTo(CalculationConfidence.NOT_AVAILABLE)
    }

    @Test
    fun buildSummary_returnsNotAvailableWhenTripHasNoLinkedFuelEntries() {
        val summary = service.buildSummary(
            trip = trip(endOdometerKm = 10_300),
            linkedEntries = emptyList(),
        )

        assertThat(summary.distanceKm).isEqualTo(300)
        assertThat(summary.averageConsumptionLitersPer100Km).isNull()
        assertThat(summary.purchaseBasedFuelLitersPer100Km).isNull()
        assertThat(summary.confidence).isEqualTo(CalculationConfidence.NOT_AVAILABLE)
    }

    @Test
    fun buildSummary_calculatesElapsedTimeWithNoBreaks() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 2 * HOUR,
                endOdometerKm = 10_120,
            ),
            linkedEntries = emptyList(),
            tripBreaks = emptyList(),
            currentTimeEpochMillis = 2 * HOUR,
        )

        assertThat(summary.totalElapsedTimeMillis).isEqualTo(2 * HOUR)
        assertThat(summary.totalBreakTimeMillis).isEqualTo(0L)
        assertThat(summary.drivingTimeMillis).isEqualTo(2 * HOUR)
    }

    @Test
    fun buildSummary_calculatesElapsedTimeWithOneBreak() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 3 * HOUR,
                endOdometerKm = 10_150,
            ),
            linkedEntries = emptyList(),
            tripBreaks = listOf(
                tripBreak(
                    id = 1,
                    startTimeEpochMillis = HOUR,
                    endTimeEpochMillis = HOUR + HALF_HOUR,
                ),
            ),
            currentTimeEpochMillis = 3 * HOUR,
        )

        assertThat(summary.totalElapsedTimeMillis).isEqualTo(3 * HOUR)
        assertThat(summary.totalBreakTimeMillis).isEqualTo(HALF_HOUR)
        assertThat(summary.drivingTimeMillis).isEqualTo(3 * HOUR - HALF_HOUR)
    }

    @Test
    fun buildSummary_calculatesElapsedTimeWithMultipleBreaks() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 5 * HOUR,
                endOdometerKm = 10_300,
            ),
            linkedEntries = emptyList(),
            tripBreaks = listOf(
                tripBreak(id = 1, startTimeEpochMillis = HOUR, endTimeEpochMillis = HOUR + HALF_HOUR),
                tripBreak(id = 2, startTimeEpochMillis = 3 * HOUR, endTimeEpochMillis = 3 * HOUR + HOUR),
            ),
            currentTimeEpochMillis = 5 * HOUR,
        )

        assertThat(summary.totalBreakTimeMillis).isEqualTo(HALF_HOUR + HOUR)
        assertThat(summary.drivingTimeMillis).isEqualTo(5 * HOUR - HALF_HOUR - HOUR)
    }

    @Test
    fun buildSummary_calculatesAverageSpeedsIncludingAndExcludingBreaks() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 4 * HOUR,
                endOdometerKm = 10_200,
            ),
            linkedEntries = emptyList(),
            tripBreaks = listOf(
                tripBreak(id = 1, startTimeEpochMillis = HOUR, endTimeEpochMillis = 2 * HOUR),
            ),
            currentTimeEpochMillis = 4 * HOUR,
        )

        assertThat(summary.averageSpeedIncludingBreaksKmH).isWithin(0.0001).of(50.0)
        assertThat(summary.averageSpeedExcludingBreaksKmH).isWithin(0.0001).of(66.66666666666667)
    }

    @Test
    fun buildSummary_usesCurrentTimeWhenEndTimeIsMissing() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = HOUR,
                endTimeEpochMillis = null,
                endOdometerKm = 10_100,
                status = TripStatus.ACTIVE,
            ),
            linkedEntries = emptyList(),
            tripBreaks = listOf(
                tripBreak(id = 1, startTimeEpochMillis = 2 * HOUR, endTimeEpochMillis = null),
            ),
            currentTimeEpochMillis = 4 * HOUR,
        )

        assertThat(summary.totalElapsedTimeMillis).isEqualTo(3 * HOUR)
        assertThat(summary.totalBreakTimeMillis).isEqualTo(2 * HOUR)
        assertThat(summary.drivingTimeMillis).isEqualTo(HOUR)
    }

    @Test
    fun buildSummary_zeroOrInvalidDurationsDoNotCrash() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 3 * HOUR,
                endTimeEpochMillis = HOUR,
                endOdometerKm = 10_100,
            ),
            linkedEntries = emptyList(),
            tripBreaks = listOf(
                tripBreak(id = 1, startTimeEpochMillis = 2 * HOUR, endTimeEpochMillis = HOUR),
            ),
            currentTimeEpochMillis = 3 * HOUR,
        )

        assertThat(summary.totalElapsedTimeMillis).isEqualTo(0L)
        assertThat(summary.totalBreakTimeMillis).isEqualTo(0L)
        assertThat(summary.drivingTimeMillis).isEqualTo(0L)
        assertThat(summary.averageSpeedIncludingBreaksKmH).isNull()
        assertThat(summary.averageSpeedExcludingBreaksKmH).isNull()
    }

    @Test
    fun buildSummary_hidesImplausibleAverageSpeeds() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 2 * 60_000L,
                endOdometerKm = 10_240,
            ),
            linkedEntries = emptyList(),
            tripBreaks = emptyList(),
            currentTimeEpochMillis = 2 * 60_000L,
        )

        assertThat(summary.averageSpeedIncludingBreaksKmH).isNull()
        assertThat(summary.averageSpeedExcludingBreaksKmH).isNull()
    }

    @Test
    fun buildSummary_sortsTimelineChronologically() {
        val summary = service.buildSummary(
            trip = trip(
                startTimeEpochMillis = 0L,
                endTimeEpochMillis = 5 * HOUR,
                endOdometerKm = 10_400,
            ),
            linkedEntries = listOf(
                entry(
                    id = 8,
                    tripId = 7,
                    litersMilliliters = 40_000,
                    totalCostCents = 7_900,
                    dateTimeEpochMillis = 3 * HOUR,
                ),
            ),
            tripBreaks = listOf(
                tripBreak(id = 2, startTimeEpochMillis = 4 * HOUR, endTimeEpochMillis = 4 * HOUR + HALF_HOUR),
                tripBreak(id = 1, startTimeEpochMillis = HOUR, endTimeEpochMillis = HOUR + HALF_HOUR),
            ),
            currentTimeEpochMillis = 5 * HOUR,
        )

        assertThat(summary.timeline.map { it.type }).containsExactly(
            TripTimelineItemType.TRIP_STARTED,
            TripTimelineItemType.BREAK,
            TripTimelineItemType.FUEL_ENTRY,
            TripTimelineItemType.BREAK,
            TripTimelineItemType.TRIP_ENDED,
        ).inOrder()
    }

    private fun trip(
        endOdometerKm: Long?,
        startTimeEpochMillis: Long = 1,
        endTimeEpochMillis: Long? = 2,
        status: TripStatus = TripStatus.COMPLETED,
    ) = Trip(
        id = 7,
        carId = 1,
        title = "Test trip",
        startLocation = "A",
        endLocation = "B",
        startOdometerKm = 10_000,
        endOdometerKm = endOdometerKm,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = endTimeEpochMillis,
        status = status,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 2,
    )

    private fun entry(
        id: Long,
        tripId: Long?,
        litersMilliliters: Long,
        totalCostCents: Long,
        dateTimeEpochMillis: Long = id,
    ) = FuelEntry(
        id = id,
        carId = 1,
        tripId = tripId,
        dateTimeEpochMillis = dateTimeEpochMillis,
        distanceInputMode = DistanceInputMode.ODOMETER,
        odometerKm = 10_000 + id,
        tripMeterKm = null,
        litersMilliliters = litersMilliliters,
        totalCostCents = totalCostCents,
        pricePerLiterMicros = 0,
        isFullTank = true,
        note = null,
        createdAtEpochMillis = id,
        updatedAtEpochMillis = id,
    )

    private fun tripBreak(
        id: Long,
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long?,
    ) = TripBreak(
        id = id,
        tripId = 7,
        startTimeEpochMillis = startTimeEpochMillis,
        endTimeEpochMillis = endTimeEpochMillis,
        note = null,
        createdAtEpochMillis = startTimeEpochMillis,
        updatedAtEpochMillis = endTimeEpochMillis ?: startTimeEpochMillis,
    )

    private companion object {
        const val HOUR = 3_600_000L
        const val HALF_HOUR = 1_800_000L
    }
}
