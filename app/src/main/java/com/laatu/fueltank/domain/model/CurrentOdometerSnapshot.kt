package com.laatu.fueltank.domain.model

data class CurrentOdometerSnapshot(
    val odometerKm: Long,
    val source: String,
)
