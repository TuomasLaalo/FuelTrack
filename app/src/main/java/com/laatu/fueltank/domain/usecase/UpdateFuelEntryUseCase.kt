package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.service.FuelEntryMutationPlanner
import javax.inject.Inject

class UpdateFuelEntryUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val fuelEntryRepository: FuelEntryRepository,
    private val fuelEntryMutationPlanner: FuelEntryMutationPlanner,
) {
    suspend operator fun invoke(
        entryId: Long,
        carId: Long,
        distanceInputMode: DistanceInputMode,
        odometerKm: Long,
        tripMeterKm: Long?,
        litersMilliliters: Long,
        totalCostCents: Long,
        pricePerLiterMicros: Long,
        stationBrand: String?,
        isFullTank: Boolean,
        note: String?,
    ) {
        val current = fuelEntryRepository.getFuelEntry(entryId) ?: return
        val car = carRepository.getPrimaryCar() ?: return
        val existingEntries = fuelEntryRepository.getEntriesForCar(carId)
        val mutationPlan = fuelEntryMutationPlanner.planUpdate(
            initialOdometerKm = car.initialOdometerKm,
            existingEntries = existingEntries,
            updatedEntry = current.copy(
                carId = carId,
                distanceInputMode = distanceInputMode,
                odometerKm = odometerKm,
                tripMeterKm = tripMeterKm,
                litersMilliliters = litersMilliliters,
                totalCostCents = totalCostCents,
                pricePerLiterMicros = pricePerLiterMicros,
                stationBrand = stationBrand?.trim().orEmpty().ifBlank { null },
                isFullTank = isFullTank,
                note = note?.trim().orEmpty().ifBlank { null },
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        fuelEntryRepository.applyMutation(
            upsertEntries = mutationPlan.upsertEntries,
            deleteEntryIds = mutationPlan.deleteEntryIds,
        )
    }
}
