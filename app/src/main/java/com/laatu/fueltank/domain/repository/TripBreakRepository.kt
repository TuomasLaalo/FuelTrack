package com.laatu.fueltank.domain.repository

import com.laatu.fueltank.domain.model.TripBreak
import kotlinx.coroutines.flow.Flow

interface TripBreakRepository {
    fun observeBreaksForTrip(tripId: Long): Flow<List<TripBreak>>
    suspend fun getBreaksForTrip(tripId: Long): List<TripBreak>
    suspend fun getBreak(breakId: Long): TripBreak?
    suspend fun getActiveBreakForTrip(tripId: Long): TripBreak?
    suspend fun upsertBreak(tripBreak: TripBreak): Long
    suspend fun deleteBreak(breakId: Long)
}
