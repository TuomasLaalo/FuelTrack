package com.laatu.fueltank.domain.usecase

import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.FuelStats
import com.laatu.fueltank.domain.service.FuelCalculationService
import javax.inject.Inject

class BuildDashboardStatsUseCase @Inject constructor(
    private val fuelCalculationService: FuelCalculationService,
) {
    operator fun invoke(entries: List<FuelEntry>): FuelStats =
        fuelCalculationService.calculateStats(entries)
}
