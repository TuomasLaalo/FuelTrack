package com.laatu.fueltank.feature.fuelhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.AppScaffold
import com.laatu.fueltank.core.ui.component.ConfirmationDialog
import com.laatu.fueltank.ui.theme.LocalFuelTankSpacing

@Composable
fun FuelHistoryRoute(
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: FuelHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScaffold(
        title = "",
        showTopBar = false,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        FuelHistoryScreen(
            padding = padding,
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onToggleSortOrder = viewModel::toggleSortOrder,
            onEditClick = onEditClick,
            onDeleteClick = viewModel::requestDelete,
            onDismissError = viewModel::dismissError,
        )
    }

    if (uiState.pendingDeleteId != null) {
        ConfirmationDialog(
            title = stringResource(R.string.fuel_history_delete_title),
            message = stringResource(R.string.fuel_history_delete_message),
            confirmLabel = stringResource(R.string.common_delete),
            dismissLabel = stringResource(R.string.common_cancel),
            destructiveConfirm = true,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteDialog,
        )
    }
}

@Composable
private fun FuelHistoryScreen(
    padding: PaddingValues,
    uiState: FuelHistoryUiState,
    onNavigateBack: () -> Unit,
    onToggleSortOrder: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onDismissError: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(spacing.large),
        contentPadding = PaddingValues(top = spacing.large, bottom = spacing.xxLarge),
    ) {
        item {
            FuelHistoryHeader(
                sortOrder = uiState.sortOrder,
                onNavigateBack = onNavigateBack,
                onToggleSortOrder = onToggleSortOrder,
            )
        }
        uiState.summary?.let { summary ->
            item {
                FuelHistorySummaryCard(summary = summary)
            }
        }
        uiState.errorMessage?.let { message ->
            item {
                HistoryErrorCard(
                    message = message,
                    onDismissError = onDismissError,
                )
            }
        }
        if (uiState.sections.isEmpty()) {
            item {
                EmptyHistoryCard()
            }
        } else {
            uiState.sections.forEach { section ->
                item {
                    MonthSectionTitle(title = section.title)
                }
                items(
                    items = section.entries,
                    key = { it.id },
                ) { entry ->
                    RefuelHistoryCard(
                        entry = entry,
                        onEditClick = { onEditClick(entry.id) },
                        onDeleteClick = { onDeleteClick(entry.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FuelHistoryHeader(
    sortOrder: FuelHistorySortOrder,
    onNavigateBack: () -> Unit,
    onToggleSortOrder: () -> Unit,
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
                modifier = Modifier.testTag(UiTestTags.FuelHistoryBack),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.fuel_history_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.fuel_history_subtitle),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            IconButton(onClick = onToggleSortOrder) {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = if (sortOrder == FuelHistorySortOrder.NEWEST_FIRST) {
                        stringResource(R.string.fuel_history_sort_newest)
                    } else {
                        stringResource(R.string.fuel_history_sort_oldest)
                    },
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun FuelHistorySummaryCard(
    summary: FuelHistorySummaryUiModel,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SummaryMetric(
                label = stringResource(R.string.fuel_history_summary_total_refuels),
                value = summary.totalRefuelsLabel,
                accent = true,
                modifier = Modifier.weight(1f),
            )
            SummaryDivider()
            SummaryMetric(
                label = stringResource(R.string.fuel_history_summary_total_quantity),
                value = summary.totalQuantityLabel,
                modifier = Modifier.weight(1f),
            )
            SummaryDivider()
            SummaryMetric(
                label = stringResource(R.string.fuel_history_summary_avg_consumption),
                value = summary.averageConsumptionLabel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Column(
        modifier = modifier.padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .width(1.dp)
            .height(42.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    )
}

@Composable
private fun MonthSectionTitle(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
    )
}

@Composable
private fun RefuelHistoryCard(
    entry: FuelHistoryItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var menuExpanded by rememberSaveable(entry.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .testTag(UiTestTags.FuelHistoryEntry),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    entry.stationBrand?.takeIf { it.isNotBlank() }?.let { stationBrand ->
                        Text(
                            text = stationBrand,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = entry.dateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.totalCostLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                    )
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(22.dp)
                                .testTag(UiTestTags.FuelHistoryEntryMoreActions),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.fuel_history_more_actions),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.common_edit)) },
                                modifier = Modifier.testTag(UiTestTags.FuelHistoryEntryEdit),
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                            )
                            DropdownMenuItem(
                                modifier = Modifier.testTag(UiTestTags.FuelHistoryEntryDelete),
                                text = {
                                        Text(
                                            text = stringResource(R.string.common_delete),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RefuelHistoryMetaBlock(
                    label = stringResource(R.string.dashboard_quantity),
                    value = entry.quantityLabel,
                    modifier = Modifier.weight(1f),
                )
                RefuelHistoryMetaBlock(
                    label = stringResource(R.string.fuel_history_price_per_unit),
                    value = entry.pricePerUnitLabel,
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                )
            }
            entry.note?.let { note ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                ) {
                    Text(
                        text = "\"$note\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RefuelHistoryMetaBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HistoryErrorCard(
    message: String,
    onDismissError: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(
                onClick = onDismissError,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(R.string.common_dismiss))
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.fuel_history_no_refuels),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.fuel_history_no_refuels_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
