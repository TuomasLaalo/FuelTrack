package com.laatu.fueltank.domain.model

data class Car(
    val id: Long,
    val name: String,
    val tankSizeMilliliters: Long,
    val initialOdometerKm: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
