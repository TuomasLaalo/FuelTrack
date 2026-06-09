package com.laatu.fueltank.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.compose.ui.window.DialogProperties
import com.laatu.fueltank.feature.appentry.AppEntryUiState
import com.laatu.fueltank.feature.appentry.AppEntryViewModel
import com.laatu.fueltank.feature.carsetup.CarSetupRoute
import com.laatu.fueltank.feature.dashboard.DashboardRoute
import com.laatu.fueltank.feature.fuelentry.AddFuelEntryRoute
import com.laatu.fueltank.feature.fuelentry.EditFuelEntryRoute
import com.laatu.fueltank.feature.fuelhistory.FuelHistoryRoute
import com.laatu.fueltank.feature.trip.EndTripRoute
import com.laatu.fueltank.feature.trip.StartTripRoute
import com.laatu.fueltank.feature.trip.TripSummaryRoute
import com.laatu.fueltank.feature.trip.TripsRoute

@Composable
fun FuelTankNavHost(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel,
) {
    val appEntryState by appEntryViewModel.uiState.collectAsStateWithLifecycle()

    if (appEntryState is AppEntryUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = when (appEntryState) {
        AppEntryUiState.NeedsCarSetup -> FuelTankDestination.CarSetup.route
        AppEntryUiState.Ready -> FuelTankDestination.Dashboard.route
        AppEntryUiState.Loading -> FuelTankDestination.CarSetup.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(FuelTankDestination.CarSetup.route) {
            CarSetupRoute(
                onCarCreated = {
                    navController.navigate(FuelTankDestination.Dashboard.route) {
                        popUpTo(FuelTankDestination.CarSetup.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(FuelTankDestination.Dashboard.route) {
            DashboardRoute(
                onAddFuelEntryClick = {
                    navController.navigate(FuelTankDestination.AddFuelEntry.route)
                },
                onHistoryClick = {
                    navController.navigate(FuelTankDestination.FuelHistory.route)
                },
                onStartTripClick = {
                    navController.navigate(FuelTankDestination.StartTrip.route)
                },
                onTripsClick = {
                    navController.navigate(FuelTankDestination.Trips.route)
                },
                onActiveTripClick = { tripId ->
                    navController.navigate(FuelTankDestination.TripSummary.createRoute(tripId))
                },
            )
        }
        dialog(
            route = FuelTankDestination.AddFuelEntry.route,
            dialogProperties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            AddFuelEntryRoute(
                onNavigateBack = navController::popBackStack,
            )
        }
        composable(FuelTankDestination.FuelHistory.route) {
            FuelHistoryRoute(
                onNavigateBack = navController::popBackStack,
                onEditClick = { entryId ->
                    navController.navigate(FuelTankDestination.EditFuelEntry.createRoute(entryId))
                },
            )
        }
        composable(FuelTankDestination.Trips.route) {
            TripsRoute(
                onNavigateBack = navController::popBackStack,
                onTripClick = { tripId ->
                    navController.navigate(FuelTankDestination.TripSummary.createRoute(tripId))
                },
            )
        }
        dialog(
            route = FuelTankDestination.StartTrip.route,
            dialogProperties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            StartTripRoute(
                onNavigateBack = navController::popBackStack,
                onTripStarted = { tripId ->
                    navController.navigate(FuelTankDestination.TripSummary.createRoute(tripId))
                },
            )
        }
        dialog(
            route = FuelTankDestination.EndTrip.route,
            dialogProperties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            EndTripRoute(
                onNavigateBack = navController::popBackStack,
                onTripEnded = { tripId ->
                    navController.navigate(FuelTankDestination.TripSummary.createRoute(tripId)) {
                        popUpTo(FuelTankDestination.Dashboard.route)
                    }
                },
            )
        }
        composable(
            route = FuelTankDestination.EditFuelEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
        ) {
            EditFuelEntryRoute(
                onNavigateBack = navController::popBackStack,
            )
        }
        composable(
            route = FuelTankDestination.TripSummary.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType }),
        ) {
            TripSummaryRoute(
                onNavigateBack = navController::popBackStack,
                onAddFuelEntryClick = {
                    navController.navigate(FuelTankDestination.AddFuelEntry.route)
                },
                onEndTripClick = {
                    navController.navigate(FuelTankDestination.EndTrip.route)
                },
            )
        }
    }
}
