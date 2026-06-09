package com.laatu.fueltank.data.mapper

import com.laatu.fueltank.data.local.entity.TripBreakEntity
import com.laatu.fueltank.domain.model.TripBreak

fun TripBreakEntity.toDomain(): TripBreak = TripBreak(
    id = id,
    tripId = tripId,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun TripBreak.toEntity(): TripBreakEntity = TripBreakEntity(
    id = id,
    tripId = tripId,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)
