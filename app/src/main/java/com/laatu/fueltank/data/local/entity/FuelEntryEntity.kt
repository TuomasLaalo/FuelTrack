package com.laatu.fueltank.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.laatu.fueltank.domain.model.DistanceInputMode

@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["carId", "dateTimeEpochMillis"]),
        Index(value = ["tripId"]),
    ],
)
data class FuelEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carId: Long,
    val tripId: Long?,
    val dateTimeEpochMillis: Long,
    val distanceInputMode: DistanceInputMode,
    val odometerKm: Long,
    val tripMeterKm: Long?,
    val litersMilliliters: Long,
    val totalCostCents: Long,
    val pricePerLiterMicros: Long,
    val stationBrand: String? = null,
    val isFullTank: Boolean,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
