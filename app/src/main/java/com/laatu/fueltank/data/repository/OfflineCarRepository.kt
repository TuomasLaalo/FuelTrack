package com.laatu.fueltank.data.repository

import com.laatu.fueltank.data.local.dao.CarDao
import com.laatu.fueltank.data.mapper.toDomain
import com.laatu.fueltank.data.mapper.toEntity
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineCarRepository @Inject constructor(
    private val carDao: CarDao,
) : CarRepository {
    override fun observePrimaryCar(): Flow<Car?> = carDao.observePrimaryCar().map { it?.toDomain() }

    override suspend fun getPrimaryCar(): Car? = carDao.getPrimaryCar()?.toDomain()

    override suspend fun upsertCar(car: Car): Long = carDao.upsertCar(car.toEntity())
}
