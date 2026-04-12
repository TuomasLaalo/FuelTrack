package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.CarRepository
import javax.inject.Inject

class ObservePrimaryCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    operator fun invoke() = carRepository.observePrimaryCar()
}
