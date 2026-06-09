package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags

@Composable
fun TripHeader(
    uiState: TripSummaryUiState,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onRenameClick: () -> Unit,
    onEditTripTimesClick: () -> Unit,
    onEndTripClick: () -> Unit,
    onNavigateBack: () -> Unit,
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
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag(UiTestTags.TripSummaryBack),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
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
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = uiState.tripDateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Box {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                    IconButton(
                        onClick = { onMenuExpandedChange(true) },
                    ) {
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
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.trip_time_edit_trip_times)) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onEditTripTimesClick()
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

@Composable
fun ActiveTripActionsRow(
    onAddFuelEntryClick: () -> Unit,
    onEndTripClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onAddFuelEntryClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = stringResource(R.string.trip_summary_add_fuel_entry),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        FilledTonalButton(
            onClick = onEndTripClick,
            modifier = Modifier
                .weight(1f)
                .testTag(UiTestTags.TripSummaryEndTrip),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.trip_summary_menu_end),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}
