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
