package com.laatu.fueltank.data.repository

import com.laatu.fueltank.data.local.dao.TripDao
import com.laatu.fueltank.data.mapper.toDomain
import com.laatu.fueltank.data.mapper.toEntity
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineTripRepository @Inject constructor(
    private val tripDao: TripDao,
) : TripRepository {
    override fun observeTripsForCar(carId: Long): Flow<List<Trip>> =
        tripDao.observeTripsForCar(carId).map { trips -> trips.map { it.toDomain() } }

    override suspend fun getTripsForCar(carId: Long): List<Trip> =
        tripDao.getTripsForCar(carId).map { it.toDomain() }

    override fun observeActiveTripForCar(carId: Long): Flow<Trip?> =
        tripDao.observeActiveTripForCar(carId).map { it?.toDomain() }

    override fun observeTrip(tripId: Long): Flow<Trip?> =
        tripDao.observeTrip(tripId).map { it?.toDomain() }

    override suspend fun getActiveTripForCar(carId: Long): Trip? =
        tripDao.getActiveTripForCar(carId)?.toDomain()

    override suspend fun getTrip(tripId: Long): Trip? =
        tripDao.getTrip(tripId)?.toDomain()

    override suspend fun upsertTrip(trip: Trip): Long {
        return if (trip.id == 0L) {
            tripDao.insertTrip(trip.toEntity())
        } else {
            tripDao.updateTrip(trip.toEntity())
            trip.id
        }
    }

    override suspend fun deleteTrip(tripId: Long) {
        tripDao.deleteTripById(tripId)
    }
}
