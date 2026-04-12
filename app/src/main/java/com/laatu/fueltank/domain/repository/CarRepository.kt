package com.laatu.fueltank.domain.repository

import com.laatu.fueltank.domain.model.Car
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun observePrimaryCar(): Flow<Car?>
    suspend fun getPrimaryCar(): Car?
    suspend fun upsertCar(car: Car): Long
}
