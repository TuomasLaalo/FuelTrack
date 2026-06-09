package com.laatu.fueltank.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.AppScaffold
import com.laatu.fueltank.ui.theme.LocalFuelTankSpacing

@Composable
fun DashboardRoute(
    onAddFuelEntryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStartTripClick: () -> Unit,
    onTripsClick: () -> Unit,
    onActiveTripClick: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    AppScaffold(
        title = "",
        showTopBar = false,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        DashboardScreen(
            padding = padding,
            uiState = uiState,
            onAddFuelEntryClick = onAddFuelEntryClick,
            onHistoryClick = onHistoryClick,
            onStartTripClick = onStartTripClick,
            onTripsClick = onTripsClick,
            onActiveTripClick = onActiveTripClick,
            onLanguageClick = { showLanguageDialog = true },
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = uiState.currentLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { language ->
                showLanguageDialog = false
                viewModel.setAppLanguage(language)
            },
        )
    }
}

@Composable
private fun DashboardScreen(
    padding: PaddingValues,
    uiState: DashboardUiState,
    onAddFuelEntryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStartTripClick: () -> Unit,
    onTripsClick: () -> Unit,
    onActiveTripClick: (Long) -> Unit,
    onLanguageClick: () -> Unit,
) {
    val spacing = LocalFuelTankSpacing.current

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
            .padding(horizontal = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.xLarge),
        contentPadding = PaddingValues(top = spacing.large, bottom = spacing.xxLarge),
    ) {
        item {
            FuelTrackHeader(
                activeTrip = uiState.activeTrip,
                onStartTripClick = onStartTripClick,
                onTripsClick = onTripsClick,
                onActiveTripClick = onActiveTripClick,
                onAddFuelEntryClick = onAddFuelEntryClick,
                onLanguageClick = onLanguageClick,
            )
        }
        item {
            VehicleContextBlock(carName = uiState.carName)
        }
        item {
            HeroMetricCard(
                value = uiState.heroConsumptionValue,
                unit = uiState.heroConsumptionUnit,
                trend = uiState.heroTrendMessage,
            )
        }
        item {
            SectionTitle(text = stringResource(R.string.dashboard_quick_stats))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.regular),
            ) {
                QuickStatCard(
                    icon = Icons.Outlined.Euro,
                    label = stringResource(R.string.dashboard_monthly_spending),
                    value = uiState.totalFuelCostLabel,
                    modifier = Modifier.weight(1f),
                )
                QuickStatCard(
                    icon = Icons.Outlined.Place,
                    label = stringResource(R.string.dashboard_distance_covered),
                    value = uiState.totalDistanceLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle(text = stringResource(R.string.dashboard_latest_refuel))
                TextButton(
                    onClick = onHistoryClick,
                    modifier = Modifier
                        .testTag(UiTestTags.DashboardViewAllRefuels)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(999.dp),
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_view_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item {
            uiState.latestRefuel?.let {
                RefuelSummaryCard(entry = it)
            } ?: CompareFuelPricesCard(
                title = stringResource(R.string.dashboard_no_refuels_yet),
                body = stringResource(R.string.dashboard_no_refuels_body),
            )
        }
        uiState.utilityCard?.let {
            item {
                CompareFuelPricesCard(
                    title = stringResource(R.string.dashboard_compare_fuel_prices),
                    body = stringResource(R.string.dashboard_compare_fuel_prices_body),
                )
            }
        }
    }
}
