package com.laatu.fueltank.domain.service

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.domain.model.CalculationConfidence
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
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
        assertThat(summary.averageConsumptionLitersPer100Km).isWithin(0.0001).of(7.777777777777778)
        assertThat(summary.confidence).isEqualTo(CalculationConfidence.LOW_CONFIDENCE)
    }

    @Test
    fun buildSummary_returnsNotAvailableWhenTripHasNoLinkedFuelEntries() {
        val summary = service.buildSummary(
            trip = trip(endOdometerKm = 10_300),
            linkedEntries = emptyList(),
        )

        assertThat(summary.distanceKm).isEqualTo(300)
        assertThat(summary.averageConsumptionLitersPer100Km).isNull()
        assertThat(summary.confidence).isEqualTo(CalculationConfidence.NOT_AVAILABLE)
    }

    private fun trip(endOdometerKm: Long?) = Trip(
        id = 7,
        carId = 1,
        title = "Test trip",
        startLocation = "A",
        endLocation = "B",
        startOdometerKm = 10_000,
        endOdometerKm = endOdometerKm,
        startTimeEpochMillis = 1,
        endTimeEpochMillis = 2,
        status = TripStatus.COMPLETED,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 2,
    )

    private fun entry(
        id: Long,
        tripId: Long?,
        litersMilliliters: Long,
        totalCostCents: Long,
    ) = FuelEntry(
        id = id,
        carId = 1,
        tripId = tripId,
        dateTimeEpochMillis = id,
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
}
