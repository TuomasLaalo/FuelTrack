package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.TripRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveActiveTripUseCase @Inject constructor(
    private val observePrimaryCarUseCase: ObservePrimaryCarUseCase,
    private val tripRepository: TripRepository,
) {
    operator fun invoke() = observePrimaryCarUseCase().flatMapLatest { car ->
        if (car == null) {
            flowOf(null)
        } else {
            tripRepository.observeActiveTripForCar(car.id)
        }
    }
}
