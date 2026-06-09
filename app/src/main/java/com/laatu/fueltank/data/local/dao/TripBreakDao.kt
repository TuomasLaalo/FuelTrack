package com.laatu.fueltank.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.laatu.fueltank.data.local.entity.TripBreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripBreakDao {
    @Query("SELECT * FROM trip_breaks WHERE tripId = :tripId ORDER BY startTimeEpochMillis ASC, id ASC")
    fun observeBreaksForTrip(tripId: Long): Flow<List<TripBreakEntity>>

    @Query("SELECT * FROM trip_breaks WHERE tripId = :tripId ORDER BY startTimeEpochMillis ASC, id ASC")
    suspend fun getBreaksForTrip(tripId: Long): List<TripBreakEntity>

    @Query("SELECT * FROM trip_breaks WHERE id = :breakId LIMIT 1")
    suspend fun getBreak(breakId: Long): TripBreakEntity?

    @Query("SELECT * FROM trip_breaks WHERE tripId = :tripId AND endTimeEpochMillis IS NULL ORDER BY startTimeEpochMillis DESC LIMIT 1")
    suspend fun getActiveBreakForTrip(tripId: Long): TripBreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreak(tripBreak: TripBreakEntity): Long

    @Update
    suspend fun updateBreak(tripBreak: TripBreakEntity)

    @Delete
    suspend fun deleteBreak(tripBreak: TripBreakEntity)

    @Query("DELETE FROM trip_breaks WHERE id = :breakId")
    suspend fun deleteBreakById(breakId: Long)
}
