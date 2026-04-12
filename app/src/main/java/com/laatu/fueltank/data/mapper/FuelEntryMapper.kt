package com.laatu.fueltank.data.mapper

import com.laatu.fueltank.data.local.entity.FuelEntryEntity
import com.laatu.fueltank.domain.model.FuelEntry

fun FuelEntryEntity.toDomain(): FuelEntry = FuelEntry(
    id = id,
    carId = carId,
    tripId = tripId,
    dateTimeEpochMillis = dateTimeEpochMillis,
    distanceInputMode = distanceInputMode,
    odometerKm = odometerKm,
    tripMeterKm = tripMeterKm,
    litersMilliliters = litersMilliliters,
    totalCostCents = totalCostCents,
    pricePerLiterMicros = pricePerLiterMicros,
    stationBrand = stationBrand,
    isFullTank = isFullTank,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun FuelEntry.toEntity(): FuelEntryEntity = FuelEntryEntity(
    id = id,
    carId = carId,
    tripId = tripId,
    dateTimeEpochMillis = dateTimeEpochMillis,
    distanceInputMode = distanceInputMode,
    odometerKm = odometerKm,
    tripMeterKm = tripMeterKm,
    litersMilliliters = litersMilliliters,
    totalCostCents = totalCostCents,
    pricePerLiterMicros = pricePerLiterMicros,
    stationBrand = stationBrand,
    isFullTank = isFullTank,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)
