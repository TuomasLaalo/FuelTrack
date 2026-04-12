package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import javax.inject.Inject

class CreateFuelEntryUseCase @Inject constructor(
    private val fuelEntryRepository: FuelEntryRepository,
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
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
        attachToActiveTrip: Boolean,
    ): Long {
        val now = System.currentTimeMillis()
        val activeTripId = if (attachToActiveTrip) {
            tripRepository.getActiveTripForCar(carId)?.id
        } else {
            null
        }
        return fuelEntryRepository.createFuelEntry(
            FuelEntry(
                id = 0,
                carId = carId,
                tripId = activeTripId,
                dateTimeEpochMillis = now,
                distanceInputMode = distanceInputMode,
                odometerKm = odometerKm,
                tripMeterKm = tripMeterKm,
                litersMilliliters = litersMilliliters,
                totalCostCents = totalCostCents,
                pricePerLiterMicros = pricePerLiterMicros,
                stationBrand = stationBrand?.trim().orEmpty().ifBlank { null },
                isFullTank = isFullTank,
                note = note?.trim().orEmpty().ifBlank { null },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }
}
