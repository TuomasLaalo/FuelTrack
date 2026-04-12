package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import javax.inject.Inject

class FuelEntryMutationPlanner @Inject constructor() {
    fun planUpdate(
        initialOdometerKm: Long,
        existingEntries: List<FuelEntry>,
        updatedEntry: FuelEntry,
    ): FuelEntryMutationPlan {
        val replacedEntries = existingEntries.map { entry ->
            if (entry.id == updatedEntry.id) updatedEntry else entry
        }

        if (replacedEntries.none { it.id == updatedEntry.id }) {
            throw FuelEntryMutationException("Fuel entry was not found.")
        }

        return FuelEntryMutationPlan(
            upsertEntries = recalculateChronologicalEntries(
                initialOdometerKm = initialOdometerKm,
                entries = replacedEntries,
            ),
        )
    }

    fun planDelete(
        initialOdometerKm: Long,
        existingEntries: List<FuelEntry>,
        deletedEntryId: Long,
    ): FuelEntryMutationPlan {
        val remainingEntries = existingEntries.filterNot { it.id == deletedEntryId }
        if (remainingEntries.size == existingEntries.size) {
            throw FuelEntryMutationException("Fuel entry was not found.")
        }

        return FuelEntryMutationPlan(
            upsertEntries = recalculateChronologicalEntries(
                initialOdometerKm = initialOdometerKm,
                entries = remainingEntries,
            ),
            deleteEntryIds = listOf(deletedEntryId),
        )
    }

    private fun recalculateChronologicalEntries(
        initialOdometerKm: Long,
        entries: List<FuelEntry>,
    ): List<FuelEntry> {
        val sortedEntries = entries.sortedWith(
            compareBy<FuelEntry> { it.dateTimeEpochMillis }
                .thenBy { it.createdAtEpochMillis }
                .thenBy { it.id }
        )

        var previousOdometerKm = initialOdometerKm
        return sortedEntries.map { entry ->
            val normalizedOdometerKm = when (entry.distanceInputMode) {
                DistanceInputMode.ODOMETER -> {
                    if (entry.odometerKm < previousOdometerKm) {
                        throw FuelEntryMutationException(
                            "Odometer cannot move backwards relative to earlier entries.",
                        )
                    }
                    entry.odometerKm
                }
                DistanceInputMode.TRIP_METER -> {
                    val tripMeterKm = entry.tripMeterKm
                        ?: throw FuelEntryMutationException("Trip meter entry is missing its distance.")
                    if (tripMeterKm < 0) {
                        throw FuelEntryMutationException("Trip meter distance cannot be negative.")
                    }
                    previousOdometerKm + tripMeterKm
                }
            }

            previousOdometerKm = normalizedOdometerKm
            entry.copy(odometerKm = normalizedOdometerKm)
        }
    }
}

data class FuelEntryMutationPlan(
    val upsertEntries: List<FuelEntry>,
    val deleteEntryIds: List<Long> = emptyList(),
)

class FuelEntryMutationException(
    override val message: String,
) : IllegalStateException(message)
