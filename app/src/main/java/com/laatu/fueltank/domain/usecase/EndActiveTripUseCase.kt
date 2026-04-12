package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.repository.TripRepository
import javax.inject.Inject

class EndActiveTripUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        distanceInputMode: DistanceInputMode,
        odometerKm: Long?,
        tripMeterKm: Long?,
        endLocation: String?,
    ): Long {
        val car = carRepository.getPrimaryCar() ?: return 0L
        val activeTrip = tripRepository.getActiveTripForCar(car.id) ?: return 0L
        val resolvedEndOdometerKm = when (distanceInputMode) {
            DistanceInputMode.ODOMETER -> odometerKm
            DistanceInputMode.TRIP_METER -> tripMeterKm?.let { activeTrip.startOdometerKm + it }
        } ?: throw IllegalStateException("Trip end odometer is required.")

        if (resolvedEndOdometerKm < activeTrip.startOdometerKm) {
            throw IllegalStateException("Trip cannot end before its starting odometer.")
        }

        val now = System.currentTimeMillis()
        return tripRepository.upsertTrip(
            activeTrip.copy(
                endLocation = endLocation?.trim().orEmpty().ifBlank { null },
                endOdometerKm = resolvedEndOdometerKm,
                endTimeEpochMillis = now,
                status = TripStatus.COMPLETED,
                updatedAtEpochMillis = now,
            ),
        )
    }
}
