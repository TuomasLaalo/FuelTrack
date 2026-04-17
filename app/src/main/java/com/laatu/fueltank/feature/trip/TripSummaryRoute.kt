package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.component.AppScaffold
import com.laatu.fueltank.ui.theme.Forest
import com.laatu.fueltank.ui.theme.ForestGlow

@Composable
fun TripSummaryRoute(
    onNavigateBack: () -> Unit,
    onAddFuelEntryClick: () -> Unit,
    onEndTripClick: () -> Unit,
    onTripsClick: () -> Unit,
    viewModel: TripSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEndTripDialog by remember { mutableStateOf(false) }
    var renameValue by remember(uiState.title) { mutableStateOf(uiState.title) }

    AppScaffold(
        title = "",
        showTopBar = false,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        TripSummaryScreen(
            padding = padding,
            uiState = uiState,
            menuExpanded = menuExpanded,
            onMenuExpandedChange = { menuExpanded = it },
            onNavigateBack = onNavigateBack,
            onRenameClick = {
                renameValue = uiState.title
                showRenameDialog = true
            },
            onEndTripClick = {
                menuExpanded = false
                showEndTripDialog = true
            },
            onAddFuelEntryClick = onAddFuelEntryClick,
            onTripsClick = onTripsClick,
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(text = stringResource(R.string.trip_summary_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.trip_summary_trip_title_label)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameTrip(renameValue)
                        showRenameDialog = false
                    },
                ) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text(text = stringResource(R.string.trip_summary_end_title)) },
            text = { Text(text = stringResource(R.string.trip_summary_end_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndTripDialog = false
                        onEndTripClick()
                    },
                ) {
                    Text(stringResource(R.string.trip_summary_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTripDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun TripSummaryScreen(
    padding: PaddingValues,
    uiState: TripSummaryUiState,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onRenameClick: () -> Unit,
    onEndTripClick: () -> Unit,
    onAddFuelEntryClick: () -> Unit,
    onTripsClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    ) {
        item {
            TripHeader(
                uiState = uiState,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = onMenuExpandedChange,
                onRenameClick = onRenameClick,
                onEndTripClick = onEndTripClick,
                onNavigateBack = onNavigateBack,
                onTripsClick = onTripsClick,
            )
        }
        item {
            TripStatusCard(uiState = uiState)
        }
        item {
            TripMetricsRow(uiState = uiState)
        }
        if (uiState.isActive) {
            item {
                ActiveTripActionsRow(
                    onAddFuelEntryClick = onAddFuelEntryClick,
                    onEndTripClick = onEndTripClick,
                )
            }
        }
        item {
            SectionHeader(title = stringResource(R.string.trip_summary_refuels_title))
        }
        if (uiState.linkedRefuels.isEmpty()) {
            item {
                EmptyRefuelsCard()
            }
        } else {
            items(uiState.linkedRefuels, key = { it.id }) { item ->
                LinkedRefuelCard(item = item)
            }
        }
        if (!uiState.isActive) {
            item {
                SectionHeader(title = stringResource(R.string.trip_summary_consumption_title))
            }
            item {
                ConsumptionCard(uiState = uiState)
            }
        }
    }
}

@Composable
private fun TripHeader(
    uiState: TripSummaryUiState,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onRenameClick: () -> Unit,
    onEndTripClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onTripsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (uiState.isActive) {
                        stringResource(R.string.trip_summary_active_header)
                    } else {
                        stringResource(R.string.trip_summary_header)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                if (uiState.headerSubtitle.isNotBlank()) {
                    Text(
                        text = uiState.headerSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onTripsClick,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(999.dp),
                ),
            ) {
                Text(
                    text = stringResource(R.string.dashboard_all_trips),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    IconButton(onClick = { onMenuExpandedChange(true) }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.trip_summary_actions),
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.trip_summary_menu_rename)) },
                        onClick = {
                            onMenuExpandedChange(false)
                            onRenameClick()
                        },
                    )
                    if (uiState.isActive) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.trip_summary_menu_end)) },
                            onClick = onEndTripClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripStatusCard(uiState: TripSummaryUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = when (uiState.statusTone) {
                        TripStatusTone.Active -> MaterialTheme.colorScheme.secondaryContainer
                        TripStatusTone.Completed -> MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = uiState.statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            TripHighlightMeta(label = stringResource(R.string.trip_summary_status_started), value = uiState.startDateTimeLabel)
            TripMetaRow(label = stringResource(R.string.trip_summary_status_start_location), value = uiState.startLocationLabel)
            if (uiState.durationLabel.isNotBlank()) {
                TripMetaRow(label = stringResource(R.string.trip_summary_status_progress), value = uiState.durationLabel)
            }
        }
    }
}

@Composable
private fun TripHighlightMeta(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            )
        }
    }
}

@Composable
private fun TripMetaRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TripMetricsRow(uiState: TripSummaryUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TripMetricTile(
            label = stringResource(R.string.trip_summary_metric_distance),
            value = uiState.distanceLabel,
            modifier = Modifier.weight(1f),
        )
        TripMetricTile(
            label = stringResource(R.string.trip_summary_metric_fuel_bought),
            value = uiState.fuelBoughtLabel,
            modifier = Modifier.weight(1f),
        )
        TripMetricTile(
            label = stringResource(R.string.trip_summary_metric_fuel_cost),
            value = uiState.fuelCostLabel,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TripMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ActiveTripActionsRow(
    onAddFuelEntryClick: () -> Unit,
    onEndTripClick: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Forest, ForestGlow),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onAddFuelEntryClick,
            modifier = Modifier
                .weight(1f)
                .background(brush = gradient, shape = RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddRoad,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.trip_summary_add_fuel_entry),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        FilledTonalButton(
            onClick = onEndTripClick,
            modifier = Modifier.weight(0.72f),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                text = stringResource(R.string.trip_summary_menu_end),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun EmptyRefuelsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
    ) {
        Text(
            text = stringResource(R.string.trip_summary_no_refuels),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Composable
private fun LinkedRefuelCard(item: TripLinkedRefuelUiModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.stationLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = item.dateTimeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = item.costLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinkedRefuelMeta(label = stringResource(R.string.dashboard_quantity), value = item.litersLabel)
            }
        }
    }
}

@Composable
private fun LinkedRefuelMeta(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ConsumptionCard(uiState: TripSummaryUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.showConsumptionValue) {
                Text(
                    text = uiState.consumptionLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    text = stringResource(R.string.trip_summary_consumption_inline, uiState.consumptionLabel),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = uiState.consumptionSupportingMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
