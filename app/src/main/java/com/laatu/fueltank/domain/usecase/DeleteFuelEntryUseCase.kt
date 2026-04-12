package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.service.FuelEntryMutationPlanner
import javax.inject.Inject

class DeleteFuelEntryUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val fuelEntryRepository: FuelEntryRepository,
    private val fuelEntryMutationPlanner: FuelEntryMutationPlanner,
) {
    suspend operator fun invoke(entryId: Long) {
        val entry = fuelEntryRepository.getFuelEntry(entryId) ?: return
        val car = carRepository.getPrimaryCar() ?: return
        val existingEntries = fuelEntryRepository.getEntriesForCar(entry.carId)
        val mutationPlan = fuelEntryMutationPlanner.planDelete(
            initialOdometerKm = car.initialOdometerKm,
            existingEntries = existingEntries,
            deletedEntryId = entryId,
        )
        fuelEntryRepository.applyMutation(
            upsertEntries = mutationPlan.upsertEntries,
            deleteEntryIds = mutationPlan.deleteEntryIds,
        )
    }
}
