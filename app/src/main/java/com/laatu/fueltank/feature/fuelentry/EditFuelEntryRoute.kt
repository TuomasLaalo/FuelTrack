package com.laatu.fueltank.feature.fuelentry

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.AppScaffold
import com.laatu.fueltank.core.ui.component.DestructiveDialogActionButtons

@Composable
fun EditFuelEntryRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditFuelEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is EditFuelEntryEvent.EntryUpdated || event is EditFuelEntryEvent.EntryDeleted) {
                onNavigateBack()
            }
        }
    }

    AppScaffold(
        title = "",
        showTopBar = false,
    ) { padding ->
        if (uiState.canEdit) {
            FuelEntryFormContent(
                uiState = uiState.formUiState,
                title = stringResource(R.string.fuel_entry_title_edit),
                subtitle = stringResource(R.string.fuel_entry_subtitle_edit),
                saveLabel = stringResource(R.string.fuel_entry_update),
                deleteLabel = stringResource(R.string.common_delete),
                modifier = Modifier.padding(padding),
                showTripAttachment = false,
                onDistanceModeChange = viewModel::onDistanceModeChange,
                onOdometerChange = viewModel::onOdometerChange,
                onTripMeterChange = viewModel::onTripMeterChange,
                onLitersChange = viewModel::onLitersChange,
                onPricePerLiterChange = viewModel::onPricePerLiterChange,
                onStationBrandChange = viewModel::onStationBrandChange,
                onNoteChange = viewModel::onNoteChange,
                onFullTankChange = viewModel::onFullTankChange,
                onCancelClick = onNavigateBack,
                onDeleteClick = viewModel::requestDelete,
                onSaveClick = viewModel::submit,
            )
        } else {
            Text(
                text = stringResource(R.string.fuel_entry_error_entry_not_found),
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
        }
    }

    if (uiState.showDeleteDialog) {
        DeleteFuelEntryDialog(
            onDeleteClick = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteDialog,
        )
    }
}

@Composable
private fun DeleteFuelEntryDialog(
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.fuel_history_delete_title)) },
        text = { Text(text = stringResource(R.string.fuel_history_delete_message)) },
        confirmButton = {
            DestructiveDialogActionButtons(
                destructiveLabel = stringResource(R.string.common_delete),
                destructiveTestTag = UiTestTags.DialogPrimaryAction,
                onDestructiveClick = onDeleteClick,
                onCancelClick = onDismiss,
            )
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )
}
