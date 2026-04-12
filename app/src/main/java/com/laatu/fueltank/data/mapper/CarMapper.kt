package com.laatu.fueltank.data.mapper

import com.laatu.fueltank.data.local.entity.CarEntity
import com.laatu.fueltank.domain.model.Car

fun CarEntity.toDomain(): Car = Car(
    id = id,
    name = name,
    tankSizeMilliliters = tankSizeMilliliters,
    initialOdometerKm = initialOdometerKm,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun Car.toEntity(): CarEntity = CarEntity(
    id = id,
    name = name,
    tankSizeMilliliters = tankSizeMilliliters,
    initialOdometerKm = initialOdometerKm,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)
