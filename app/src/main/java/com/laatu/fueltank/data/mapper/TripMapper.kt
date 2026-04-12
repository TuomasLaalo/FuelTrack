package com.laatu.fueltank.data.mapper

import com.laatu.fueltank.data.local.entity.TripEntity
import com.laatu.fueltank.domain.model.Trip

fun TripEntity.toDomain(): Trip = Trip(
    id = id,
    carId = carId,
    title = title,
    startLocation = startLocation,
    endLocation = endLocation,
    startOdometerKm = startOdometerKm,
    endOdometerKm = endOdometerKm,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = status,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun Trip.toEntity(): TripEntity = TripEntity(
    id = id,
    carId = carId,
    title = title,
    startLocation = startLocation,
    endLocation = endLocation,
    startOdometerKm = startOdometerKm,
    endOdometerKm = endOdometerKm,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = status,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)
