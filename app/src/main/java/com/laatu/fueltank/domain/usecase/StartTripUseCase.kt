package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import com.laatu.fueltank.domain.repository.TripRepository
import com.laatu.fueltank.domain.service.CurrentOdometerService
import javax.inject.Inject

class StartTripUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val fuelEntryRepository: FuelEntryRepository,
    private val tripRepository: TripRepository,
    private val currentOdometerService: CurrentOdometerService,
) {
    suspend operator fun invoke(
        title: String?,
        startLocation: String?,
    ): Long {
        val car = carRepository.getPrimaryCar() ?: return 0L
        if (tripRepository.getActiveTripForCar(car.id) != null) {
            throw IllegalStateException("Only one trip can be active at a time.")
        }

        val fuelEntries = fuelEntryRepository.getEntriesForCar(car.id)
        val trips = tripRepository.getTripsForCar(car.id)
        val now = System.currentTimeMillis()

        val currentOdometer = currentOdometerService.resolve(
            car = car,
            fuelEntries = fuelEntries,
            trips = trips,
        )

        return tripRepository.upsertTrip(
            Trip(
                id = 0,
                carId = car.id,
                title = title?.trim().orEmpty().ifBlank { null },
                startLocation = startLocation?.trim().orEmpty().ifBlank { null },
                endLocation = null,
                startOdometerKm = currentOdometer.odometerKm,
                endOdometerKm = null,
                startTimeEpochMillis = now,
                endTimeEpochMillis = null,
                status = TripStatus.ACTIVE,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }
}
