package com.laatu.fueltank.domain.service

import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.CurrentOdometerSnapshot
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripStatus
import javax.inject.Inject

class CurrentOdometerService @Inject constructor() {
    fun resolve(
        car: Car,
        fuelEntries: List<FuelEntry>,
        trips: List<Trip>,
    ): CurrentOdometerSnapshot {
        // The onboarding odometer is only a seed value. The current odometer is derived
        // from the newest known fuel-entry or trip history so it does not drift from reality.
        val latestFuelOdometer = fuelEntries.maxOfOrNull { it.odometerKm }
        val latestTripOdometer = trips.maxOfOrNull { trip ->
            when (trip.status) {
                TripStatus.ACTIVE -> trip.startOdometerKm
                TripStatus.COMPLETED -> trip.endOdometerKm ?: trip.startOdometerKm
            }
        }

        val odometerKm = listOfNotNull(car.initialOdometerKm, latestFuelOdometer, latestTripOdometer).maxOrNull()
            ?: car.initialOdometerKm

        val source = when (odometerKm) {
            latestFuelOdometer -> "fuel-entry history"
            latestTripOdometer -> "trip history"
            else -> "car setup"
        }

        return CurrentOdometerSnapshot(
            odometerKm = odometerKm,
            source = source,
        )
    }
}
