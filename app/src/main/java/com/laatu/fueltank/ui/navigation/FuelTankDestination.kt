package com.laatu.fueltank.ui.navigation

sealed interface FuelTankDestination {
    val route: String

    data object CarSetup : FuelTankDestination {
        override val route = "car-setup"
    }

    data object Dashboard : FuelTankDestination {
        override val route = "dashboard"
    }

    data object AddFuelEntry : FuelTankDestination {
        override val route = "add-fuel-entry"
    }

    data object FuelHistory : FuelTankDestination {
        override val route = "fuel-history"
    }

    data object Trips : FuelTankDestination {
        override val route = "trips"
    }

    data object StartTrip : FuelTankDestination {
        override val route = "start-trip"
    }

    data object EndTrip : FuelTankDestination {
        override val route = "end-trip"
    }

    data object TripSummary : FuelTankDestination {
        override val route = "trip-summary/{tripId}"

        fun createRoute(tripId: Long): String = "trip-summary/$tripId"
    }

    data object EditFuelEntry : FuelTankDestination {
        override val route = "edit-fuel-entry/{entryId}"

        fun createRoute(entryId: Long): String = "edit-fuel-entry/$entryId"
    }
}
