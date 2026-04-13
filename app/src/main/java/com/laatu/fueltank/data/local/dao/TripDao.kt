package com.laatu.fueltank.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.laatu.fueltank.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE carId = :carId ORDER BY startTimeEpochMillis DESC")
    fun observeTripsForCar(carId: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE carId = :carId ORDER BY startTimeEpochMillis DESC")
    suspend fun getTripsForCar(carId: Long): List<TripEntity>

    @Query("SELECT * FROM trips WHERE carId = :carId AND status = 'ACTIVE' ORDER BY startTimeEpochMillis DESC LIMIT 1")
    fun observeActiveTripForCar(carId: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun observeTrip(tripId: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE carId = :carId AND status = 'ACTIVE' ORDER BY startTimeEpochMillis DESC LIMIT 1")
    suspend fun getActiveTripForCar(carId: Long): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTrip(tripId: Long): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: Long)
}
