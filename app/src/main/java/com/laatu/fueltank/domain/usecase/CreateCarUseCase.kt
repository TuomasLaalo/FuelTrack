package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.repository.CarRepository
import javax.inject.Inject

class CreateCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    suspend operator fun invoke(
        name: String,
        tankSizeMilliliters: Long,
        initialOdometerKm: Long,
    ): Long {
        val now = System.currentTimeMillis()
        return carRepository.upsertCar(
            Car(
                id = 1L,
                name = name,
                tankSizeMilliliters = tankSizeMilliliters,
                initialOdometerKm = initialOdometerKm,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }
}
