package com.laatu.fueltank.data.repository

import androidx.room.withTransaction
import com.laatu.fueltank.data.local.FuelTankDatabase
import com.laatu.fueltank.data.local.dao.FuelEntryDao
import com.laatu.fueltank.data.mapper.toDomain
import com.laatu.fueltank.data.mapper.toEntity
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.repository.FuelEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFuelEntryRepository @Inject constructor(
    private val database: FuelTankDatabase,
    private val fuelEntryDao: FuelEntryDao,
) : FuelEntryRepository {
    override fun observeEntriesForCar(carId: Long): Flow<List<FuelEntry>> =
        fuelEntryDao.observeEntriesForCar(carId).map { entries -> entries.map { it.toDomain() } }

    override suspend fun getEntriesForCar(carId: Long): List<FuelEntry> =
        fuelEntryDao.getEntriesForCar(carId).map { it.toDomain() }

    override suspend fun createFuelEntry(fuelEntry: FuelEntry): Long =
        fuelEntryDao.insertFuelEntry(fuelEntry.toEntity())

    override suspend fun getFuelEntry(entryId: Long): FuelEntry? =
        fuelEntryDao.getFuelEntry(entryId)?.toDomain()

    override suspend fun updateFuelEntry(fuelEntry: FuelEntry) {
        fuelEntryDao.updateFuelEntry(fuelEntry.toEntity())
    }

    override suspend fun deleteFuelEntry(entryId: Long) {
        fuelEntryDao.deleteFuelEntry(entryId)
    }

    override suspend fun applyMutation(upsertEntries: List<FuelEntry>, deleteEntryIds: List<Long>) {
        database.withTransaction {
            if (deleteEntryIds.isNotEmpty()) {
                fuelEntryDao.deleteFuelEntries(deleteEntryIds)
            }
            if (upsertEntries.isNotEmpty()) {
                fuelEntryDao.upsertFuelEntries(upsertEntries.map { it.toEntity() })
            }
        }
    }
}
