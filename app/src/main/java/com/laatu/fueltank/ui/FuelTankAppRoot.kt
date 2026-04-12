package com.laatu.fueltank.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.laatu.fueltank.feature.appentry.AppEntryViewModel
import com.laatu.fueltank.ui.navigation.FuelTankNavHost

@Composable
fun FuelTankAppRoot() {
    val navController = rememberNavController()
    val appEntryViewModel: AppEntryViewModel = hiltViewModel()
    FuelTankNavHost(
        navController = navController,
        appEntryViewModel = appEntryViewModel,
    )
}
