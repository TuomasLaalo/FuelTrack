package com.laatu.fueltank.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags

@Composable
internal fun FuelTrackHeader(
    activeTrip: DashboardActiveTripUiModel?,
    onStartTripClick: () -> Unit,
    onTripsClick: () -> Unit,
    onActiveTripClick: (Long) -> Unit,
    onAddFuelEntryClick: () -> Unit,
    onLanguageClick: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag(UiTestTags.DashboardMenu),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.dashboard_menu),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dashboard_menu_language)) },
                            modifier = Modifier.testTag(UiTestTags.DashboardMenuLanguage),
                            onClick = {
                                menuExpanded = false
                                onLanguageClick()
                            },
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.dashboard_brand),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (activeTrip == null) {
                    TripHeaderActionButton(onClick = onStartTripClick)
                }
                IconButton(
                    onClick = onAddFuelEntryClick,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag(UiTestTags.DashboardAddRefill)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(14.dp),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.dashboard_add_refill),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
        if (activeTrip != null) {
            ActiveTripStatusCard(
                activeTrip = activeTrip,
                onClick = { onActiveTripClick(activeTrip.id) },
            )
        } else {
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
        }
    }
}

@Composable
private fun ActiveTripStatusCard(
    activeTrip: DashboardActiveTripUiModel,
    onClick: () -> Unit,
) {
    val warmSurface = Color(0xFF2E281B)
    val warmAccent = Color(0xFFD7B56D)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = warmSurface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    warmAccent.copy(alpha = 0.86f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                                ),
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null,
                        tint = Color(0xFF14110A),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(11.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = RoundedCornerShape(999.dp),
                            )
                            .border(
                                width = 2.dp,
                                color = warmAccent,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_active_trip_status),
                        style = MaterialTheme.typography.labelSmall,
                        color = warmAccent,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = activeTrip.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFF3E9D3),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = activeTrip.locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCFC1A3),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = warmAccent.copy(alpha = 0.16f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = warmAccent,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = activeTrip.elapsedLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFF3E9D3),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun TripHeaderActionButton(
    onClick: () -> Unit,
) {
    val shape: Shape = RoundedCornerShape(14.dp)

    FilledTonalButton(
        onClick = onClick,
        shape = shape,
        modifier = Modifier
            .size(46.dp)
            .testTag(UiTestTags.DashboardStartTrip),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.AddRoad,
            contentDescription = stringResource(R.string.dashboard_trip_button_start),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
internal fun VehicleContextBlock(
    carName: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(R.string.dashboard_active_vehicle),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (carName.isBlank()) stringResource(R.string.dashboard_default_vehicle_name) else carName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
