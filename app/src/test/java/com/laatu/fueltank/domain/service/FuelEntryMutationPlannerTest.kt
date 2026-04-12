package com.laatu.fueltank.domain.service

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FuelEntryMutationPlannerTest {
    private val planner = FuelEntryMutationPlanner()

    @Test
    fun planUpdate_recalculatesLaterTripMeterEntriesAfterOdometerChange() {
        val entries = listOf(
            entry(id = 1, dateTimeEpochMillis = 1, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_100),
            entry(id = 2, dateTimeEpochMillis = 2, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_200, tripMeterKm = 100),
            entry(id = 3, dateTimeEpochMillis = 3, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_320, tripMeterKm = 120),
        )

        val plan = planner.planUpdate(
            initialOdometerKm = 10_000,
            existingEntries = entries,
            updatedEntry = entries.first().copy(odometerKm = 10_250),
        )

        assertThat(plan.deleteEntryIds).isEmpty()
        assertThat(plan.upsertEntries.map { it.odometerKm }).containsExactly(10_250L, 10_350L, 10_470L).inOrder()
    }

    @Test
    fun planDelete_recalculatesTripMeterChainFromPreviousBaseline() {
        val entries = listOf(
            entry(id = 1, dateTimeEpochMillis = 1, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_100),
            entry(id = 2, dateTimeEpochMillis = 2, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_300),
            entry(id = 3, dateTimeEpochMillis = 3, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_450, tripMeterKm = 150),
        )

        val plan = planner.planDelete(
            initialOdometerKm = 10_000,
            existingEntries = entries,
            deletedEntryId = 2,
        )

        assertThat(plan.deleteEntryIds).containsExactly(2L)
        assertThat(plan.upsertEntries.map { it.id to it.odometerKm }).containsExactly(
            1L to 10_100L,
            3L to 10_250L,
        ).inOrder()
    }

    @Test(expected = FuelEntryMutationException::class)
    fun planUpdate_throwsWhenLaterManualOdometerWouldMoveBackwards() {
        val entries = listOf(
            entry(id = 1, dateTimeEpochMillis = 1, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_100),
            entry(id = 2, dateTimeEpochMillis = 2, distanceInputMode = DistanceInputMode.TRIP_METER, odometerKm = 10_200, tripMeterKm = 100),
            entry(id = 3, dateTimeEpochMillis = 3, distanceInputMode = DistanceInputMode.ODOMETER, odometerKm = 10_250),
        )

        planner.planUpdate(
            initialOdometerKm = 10_000,
            existingEntries = entries,
            updatedEntry = entries.first().copy(odometerKm = 10_220),
        )
    }

    private fun entry(
        id: Long,
        dateTimeEpochMillis: Long,
        distanceInputMode: DistanceInputMode,
        odometerKm: Long,
        tripMeterKm: Long? = null,
    ) = FuelEntry(
        id = id,
        carId = 1,
        tripId = null,
        dateTimeEpochMillis = dateTimeEpochMillis,
        distanceInputMode = distanceInputMode,
        odometerKm = odometerKm,
        tripMeterKm = tripMeterKm,
        litersMilliliters = 40_000,
        totalCostCents = 6_000,
        pricePerLiterMicros = 1_500_000,
        isFullTank = true,
        note = null,
        createdAtEpochMillis = dateTimeEpochMillis,
        updatedAtEpochMillis = dateTimeEpochMillis,
    )
}
