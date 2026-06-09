package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.repository.TripBreakRepository
import javax.inject.Inject

class DeleteTripBreakUseCase @Inject constructor(
    private val tripBreakRepository: TripBreakRepository,
) {
    suspend operator fun invoke(breakId: Long) {
        tripBreakRepository.deleteBreak(breakId)
    }
}
