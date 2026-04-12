package com.laatu.fueltank.domain.service

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import org.junit.Test

class ConsumptionSegmentBuilderTest {
    private val builder = ConsumptionSegmentBuilder()

    @Test
    fun build_returnsReliableSegment_betweenTwoFullTanks() {
        val segments = builder.build(
            listOf(
                entry(id = 1, odometerKm = 100_000, litersMilliliters = 40_000, totalCostCents = 7_000, isFullTank = true),
                entry(id = 2, odometerKm = 100_500, litersMilliliters = 20_000, totalCostCents = 3_500, isFullTank = false),
                entry(id = 3, odometerKm = 101_000, litersMilliliters = 35_000, totalCostCents = 6_000, isFullTank = true),
            ),
        )

        assertThat(segments).hasSize(1)
        assertThat(segments.first().distanceKm).isEqualTo(1_000)
        assertThat(segments.first().fuelUsedMilliliters).isEqualTo(55_000)
        assertThat(segments.first().costCents).isEqualTo(9_500)
    }

    @Test
    fun build_excludesStartingAnchorFuel() {
        val segments = builder.build(
            listOf(
                entry(id = 1, odometerKm = 10_000, litersMilliliters = 41_000, totalCostCents = 7_100, isFullTank = true),
                entry(id = 2, odometerKm = 10_700, litersMilliliters = 30_000, totalCostCents = 5_100, isFullTank = true),
            ),
        )

        assertThat(segments).hasSize(1)
        assertThat(segments.first().fuelUsedMilliliters).isEqualTo(30_000)
        assertThat(segments.first().costCents).isEqualTo(5_100)
    }

    @Test
    fun build_returnsEmpty_whenEndingAnchorMissing() {
        val segments = builder.build(
            listOf(
                entry(id = 1, odometerKm = 20_000, litersMilliliters = 38_000, totalCostCents = 6_000, isFullTank = true),
                entry(id = 2, odometerKm = 20_500, litersMilliliters = 22_000, totalCostCents = 3_700, isFullTank = false),
            ),
        )

        assertThat(segments).isEmpty()
    }

    private fun entry(
        id: Long,
        odometerKm: Long,
        litersMilliliters: Long,
        totalCostCents: Long,
        isFullTank: Boolean,
    ) = FuelEntry(
        id = id,
        carId = 1,
        tripId = null,
        dateTimeEpochMillis = id,
        distanceInputMode = DistanceInputMode.ODOMETER,
        odometerKm = odometerKm,
        tripMeterKm = null,
        litersMilliliters = litersMilliliters,
        totalCostCents = totalCostCents,
        pricePerLiterMicros = 0,
        isFullTank = isFullTank,
        note = null,
        createdAtEpochMillis = id,
        updatedAtEpochMillis = id,
    )
}
