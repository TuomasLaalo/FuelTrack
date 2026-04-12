package com.laatu.fueltank.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.laatu.fueltank.data.local.entity.FuelEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries WHERE carId = :carId ORDER BY dateTimeEpochMillis DESC")
    fun observeEntriesForCar(carId: Long): Flow<List<FuelEntryEntity>>

    @Query("SELECT * FROM fuel_entries WHERE carId = :carId ORDER BY dateTimeEpochMillis ASC, createdAtEpochMillis ASC, id ASC")
    suspend fun getEntriesForCar(carId: Long): List<FuelEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelEntry(fuelEntry: FuelEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFuelEntries(fuelEntries: List<FuelEntryEntity>)

    @Query("SELECT * FROM fuel_entries WHERE id = :entryId LIMIT 1")
    suspend fun getFuelEntry(entryId: Long): FuelEntryEntity?

    @Update
    suspend fun updateFuelEntry(fuelEntry: FuelEntryEntity)

    @Query("DELETE FROM fuel_entries WHERE id = :entryId")
    suspend fun deleteFuelEntry(entryId: Long)

    @Query("DELETE FROM fuel_entries WHERE id IN (:entryIds)")
    suspend fun deleteFuelEntries(entryIds: List<Long>)
}
