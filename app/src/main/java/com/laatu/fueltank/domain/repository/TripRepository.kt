package com.laatu.fueltank.domain.repository

import com.laatu.fueltank.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTripsForCar(carId: Long): Flow<List<Trip>>
    fun observeActiveTripForCar(carId: Long): Flow<Trip?>
    fun observeTrip(tripId: Long): Flow<Trip?>
    suspend fun getTripsForCar(carId: Long): List<Trip>
    suspend fun getActiveTripForCar(carId: Long): Trip?
    suspend fun getTrip(tripId: Long): Trip?
    suspend fun upsertTrip(trip: Trip): Long
}
