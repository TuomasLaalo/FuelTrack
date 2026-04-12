package com.laatu.fueltank.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.laatu.fueltank.domain.model.TripStatus

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["carId", "startTimeEpochMillis"]),
    ],
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carId: Long,
    val title: String?,
    val startLocation: String?,
    val endLocation: String?,
    val startOdometerKm: Long,
    val endOdometerKm: Long?,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val status: TripStatus,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
