package com.laatu.fueltank.domain.repository

import com.laatu.fueltank.domain.model.FuelEntry
import kotlinx.coroutines.flow.Flow

interface FuelEntryRepository {
    fun observeEntriesForCar(carId: Long): Flow<List<FuelEntry>>
    suspend fun getEntriesForCar(carId: Long): List<FuelEntry>
    suspend fun createFuelEntry(fuelEntry: FuelEntry): Long
    suspend fun getFuelEntry(entryId: Long): FuelEntry?
    suspend fun updateFuelEntry(fuelEntry: FuelEntry)
    suspend fun deleteFuelEntry(entryId: Long)
    suspend fun applyMutation(
        upsertEntries: List<FuelEntry>,
        deleteEntryIds: List<Long> = emptyList(),
    )
}
