package com.laatu.fueltank.feature.fuelentry

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.core.ui.component.AppScaffold

@Composable
fun EditFuelEntryRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditFuelEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is EditFuelEntryEvent.EntryUpdated) {
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
                title = "Edit Refuel",
                subtitle = "Adjust the entry while keeping the fuel history consistent.",
                saveLabel = "Update entry",
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
                onSaveClick = viewModel::submit,
            )
        } else {
            Text(
                text = "Fuel entry was not found.",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
        }
    }
}
