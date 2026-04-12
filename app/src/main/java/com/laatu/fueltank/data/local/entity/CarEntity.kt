package com.laatu.fueltank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val tankSizeMilliliters: Long,
    val initialOdometerKm: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
