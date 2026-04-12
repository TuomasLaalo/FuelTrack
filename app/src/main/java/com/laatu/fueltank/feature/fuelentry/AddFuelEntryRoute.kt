package com.laatu.fueltank.feature.fuelentry

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.core.ui.component.ConfirmationDialog

@Composable
fun AddFuelEntryRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddFuelEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.requestCancel()
    }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                AddFuelEntryEvent.EntrySaved,
                AddFuelEntryEvent.Cancelled -> onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f)),
            contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 620.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ),
        ) {
            FuelEntryFormContent(
                uiState = uiState,
                title = "Add Fuel Entry",
                subtitle = "Log your latest refuel details",
                saveLabel = "Save Entry",
                discardLabel = "Discard",
                modifier = Modifier.padding(top = 12.dp),
                showTripAttachment = true,
                onDistanceModeChange = viewModel::onDistanceModeChange,
                onOdometerChange = viewModel::onOdometerChange,
                onTripMeterChange = viewModel::onTripMeterChange,
                onLitersChange = viewModel::onLitersChange,
                onPricePerLiterChange = viewModel::onPricePerLiterChange,
                onStationBrandChange = viewModel::onStationBrandChange,
                onNoteChange = viewModel::onNoteChange,
                onFullTankChange = viewModel::onFullTankChange,
                onAttachToActiveTripChange = viewModel::onAttachToActiveTripChange,
                onCancelClick = viewModel::requestCancel,
                onDiscardClick = viewModel::discardEntry,
                onSaveClick = viewModel::submit,
                modalStyle = true,
            )
        }
    }

    if (uiState.showDiscardDialog) {
        ConfirmationDialog(
            title = "Discard refuel entry?",
            message = "Your current input will be lost.",
            confirmLabel = "Discard",
            dismissLabel = "Keep editing",
            onConfirm = viewModel::confirmDiscard,
            onDismiss = viewModel::dismissDiscardDialog,
        )
    }
}
