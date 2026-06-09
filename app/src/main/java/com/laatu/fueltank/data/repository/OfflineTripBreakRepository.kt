package com.laatu.fueltank.data.repository

import com.laatu.fueltank.data.local.dao.TripBreakDao
import com.laatu.fueltank.data.mapper.toDomain
import com.laatu.fueltank.data.mapper.toEntity
import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.repository.TripBreakRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineTripBreakRepository @Inject constructor(
    private val tripBreakDao: TripBreakDao,
) : TripBreakRepository {
    override fun observeBreaksForTrip(tripId: Long): Flow<List<TripBreak>> =
        tripBreakDao.observeBreaksForTrip(tripId).map { breaks -> breaks.map { it.toDomain() } }

    override suspend fun getBreaksForTrip(tripId: Long): List<TripBreak> =
        tripBreakDao.getBreaksForTrip(tripId).map { it.toDomain() }

    override suspend fun getBreak(breakId: Long): TripBreak? =
        tripBreakDao.getBreak(breakId)?.toDomain()

    override suspend fun getActiveBreakForTrip(tripId: Long): TripBreak? =
        tripBreakDao.getActiveBreakForTrip(tripId)?.toDomain()

    override suspend fun upsertBreak(tripBreak: TripBreak): Long {
        return if (tripBreak.id == 0L) {
            tripBreakDao.insertBreak(tripBreak.toEntity())
        } else {
            tripBreakDao.updateBreak(tripBreak.toEntity())
            tripBreak.id
        }
    }

    override suspend fun deleteBreak(breakId: Long) {
        tripBreakDao.deleteBreakById(breakId)
    }
}
