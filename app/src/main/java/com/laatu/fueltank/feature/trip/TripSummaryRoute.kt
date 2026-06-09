package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.AppScaffold
import com.laatu.fueltank.core.ui.component.CompactSectionHeader

@Composable
fun TripSummaryRoute(
    onNavigateBack: () -> Unit,
    onAddFuelEntryClick: () -> Unit,
    onEndTripClick: () -> Unit,
    viewModel: TripSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEndTripDialog by remember { mutableStateOf(false) }
    var showTripTimesDialog by remember { mutableStateOf(false) }
    var breakDialogTarget by remember { mutableStateOf<TripBreakUiModel?>(null) }
    var showBreakDialog by remember { mutableStateOf(false) }
    var breakDeleteTarget by remember { mutableStateOf<TripBreakUiModel?>(null) }
    var renameValue by remember(uiState.title) { mutableStateOf(uiState.title) }
    var tripStartValue by remember(uiState.tripStartInput) { mutableStateOf(uiState.tripStartInput) }
    var tripEndValue by remember(uiState.tripEndInput) { mutableStateOf(uiState.tripEndInput) }
    var breakStartValue by remember { mutableStateOf("") }
    var breakEndValue by remember { mutableStateOf("") }
    var breakNoteValue by remember { mutableStateOf("") }

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
            onEditTripTimesClick = {
                viewModel.clearActionError()
                tripStartValue = uiState.tripStartInput
                tripEndValue = uiState.tripEndInput
                showTripTimesDialog = true
            },
            onEndTripClick = {
                menuExpanded = false
                showEndTripDialog = true
            },
            onAddFuelEntryClick = onAddFuelEntryClick,
            onStartBreakClick = viewModel::startBreak,
            onEndBreakClick = viewModel::endBreak,
            onAddBreakClick = {
                viewModel.clearActionError()
                breakDialogTarget = null
                breakStartValue = uiState.newBreakStartInput
                breakEndValue = uiState.newBreakEndInput
                breakNoteValue = ""
                showBreakDialog = true
            },
            onEditBreakClick = { tripBreak ->
                viewModel.clearActionError()
                breakDialogTarget = tripBreak
                breakStartValue = tripBreak.startInput
                breakEndValue = tripBreak.endInput
                breakNoteValue = tripBreak.note
                showBreakDialog = true
            },
        )
    }

    if (showRenameDialog) {
        RenameTripDialog(
            titleValue = renameValue,
            onTitleChange = { renameValue = it },
            onSaveClick = {
                viewModel.renameTrip(renameValue)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showEndTripDialog) {
        ConfirmEndTripDialog(
            onContinueClick = {
                showEndTripDialog = false
                onEndTripClick()
            },
            onDismiss = { showEndTripDialog = false },
        )
    }

    if (showTripTimesDialog) {
        EditTripTimesDialog(
            startValue = tripStartValue,
            endValue = tripEndValue,
            allowClearEnd = uiState.isActive,
            onStartChange = { tripStartValue = it },
            onEndChange = { tripEndValue = it },
            onSaveClick = {
                viewModel.saveTripTimes(tripStartValue, tripEndValue)
                showTripTimesDialog = false
            },
            onDismiss = { showTripTimesDialog = false },
        )
    }

    if (showBreakDialog) {
        EditBreakDialog(
            target = breakDialogTarget,
            startValue = breakStartValue,
            endValue = breakEndValue,
            noteValue = breakNoteValue,
            onStartChange = { breakStartValue = it },
            onEndChange = { breakEndValue = it },
            onNoteChange = { breakNoteValue = it },
            onSaveClick = {
                viewModel.saveBreak(
                    breakId = breakDialogTarget?.id,
                    startInput = breakStartValue,
                    endInput = breakEndValue,
                    note = breakNoteValue,
                )
                showBreakDialog = false
            },
            onDeleteClick = {
                breakDialogTarget?.let { target ->
                    showBreakDialog = false
                    breakDeleteTarget = target
                }
            },
            onDismiss = { showBreakDialog = false },
        )
    }

    breakDeleteTarget?.let { target ->
        DeleteBreakDialog(
            onDeleteClick = {
                viewModel.deleteBreak(target.id)
                breakDeleteTarget = null
            },
            onDismiss = { breakDeleteTarget = null },
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
    onEditTripTimesClick: () -> Unit,
    onEndTripClick: () -> Unit,
    onAddFuelEntryClick: () -> Unit,
    onStartBreakClick: () -> Unit,
    onEndBreakClick: () -> Unit,
    onAddBreakClick: () -> Unit,
    onEditBreakClick: (TripBreakUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiTestTags.TripSummaryScreen)
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
                onEditTripTimesClick = onEditTripTimesClick,
                onEndTripClick = onEndTripClick,
                onNavigateBack = onNavigateBack,
            )
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
            TripRouteHeroCard(uiState = uiState)
        }
        item {
            TripTimeDistributionCard(
                uiState = uiState,
                onStartBreakClick = onStartBreakClick,
                onEndBreakClick = onEndBreakClick,
                onAddBreakClick = onAddBreakClick,
                onEditTripTimesClick = onEditTripTimesClick,
            )
        }
        if (uiState.timeline.isNotEmpty()) {
            item {
                TripTimelineCard(
                    items = uiState.timeline,
                    breaks = uiState.breaks,
                    onEditBreakClick = onEditBreakClick,
                )
            }
        }
        item {
            if (uiState.linkedRefuels.isNotEmpty()) {
                CompactSectionHeader(title = stringResource(R.string.trip_summary_refuels_title))
            }
        }
        if (uiState.linkedRefuels.isNotEmpty()) {
            items(uiState.linkedRefuels, key = { it.id }) { item ->
                LinkedRefuelCard(item = item)
            }
        }
    }
}
