package com.laatu.fueltank.feature.trip

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.component.FormField
import com.laatu.fueltank.core.ui.component.ConfirmationDialog
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.ui.theme.Forest
import com.laatu.fueltank.ui.theme.ForestGlow
import com.laatu.fueltank.ui.theme.LocalFuelTankSpacing
import com.laatu.fueltank.ui.theme.rememberFuelTrackModalColors

@Composable
fun EndTripRoute(
    onNavigateBack: () -> Unit,
    onTripEnded: (Long) -> Unit,
    viewModel: EndTripViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val isDirty = uiState.odometerKm.isNotBlank() || uiState.tripMeterKm.isNotBlank() || uiState.endLocation.isNotBlank()

    fun requestClose() {
        if (isDirty && !uiState.isSaving) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        requestClose()
    }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is EndTripEvent.TripEnded) {
                onTripEnded(event.tripId)
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
                .heightIn(min = 560.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ),
        ) {
            EndTripSheet(
                uiState = uiState,
                modifier = Modifier.padding(top = 12.dp),
                onDistanceModeChange = viewModel::onDistanceModeChange,
                onOdometerChange = viewModel::onOdometerChange,
                onTripMeterChange = viewModel::onTripMeterChange,
                onEndLocationChange = viewModel::onEndLocationChange,
                onCancelClick = ::requestClose,
                onEndTripClick = viewModel::submit,
            )
        }
    }

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.end_trip_discard_title),
            message = stringResource(R.string.end_trip_discard_message),
            confirmLabel = stringResource(R.string.fuel_entry_discard),
            dismissLabel = stringResource(R.string.fuel_entry_keep_editing),
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
}

@Composable
private fun EndTripSheet(
    uiState: EndTripUiState,
    modifier: Modifier = Modifier,
    onDistanceModeChange: (DistanceInputMode) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTripMeterChange: (String) -> Unit,
    onEndLocationChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onEndTripClick: () -> Unit,
) {
    val spacing = LocalFuelTankSpacing.current
    val modalColors = rememberFuelTrackModalColors(modalStyle = true)

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = modalColors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.large, vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EndTripHeader(
                title = stringResource(R.string.end_trip_title),
                subtitle = stringResource(R.string.end_trip_subtitle),
                onCancelClick = onCancelClick,
                primaryText = modalColors.primaryText,
                secondaryText = modalColors.secondaryText,
            )

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = modalColors.fieldSurface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = uiState.tripTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = modalColors.secondaryText,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.end_trip_started_at, uiState.startOdometerLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = modalColors.primaryText,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            EndTripDistanceToggle(
                selectedMode = uiState.distanceInputMode,
                onModeChange = onDistanceModeChange,
                primaryText = modalColors.primaryText,
                secondaryText = modalColors.secondaryText,
                surfaceColor = modalColors.toggleContainer,
                selectedSurfaceColor = modalColors.toggleSelected,
            )

            if (uiState.distanceInputMode == DistanceInputMode.ODOMETER) {
                FormField(
                    value = uiState.odometerKm,
                    onValueChange = onOdometerChange,
                    label = stringResource(R.string.fuel_entry_odometer_km),
                    placeholder = stringResource(R.string.fuel_entry_odometer_placeholder),
                    filled = true,
                    isError = uiState.odometerError != null,
                    filledContainerColor = modalColors.fieldSurface,
                    filledTextColor = modalColors.primaryText,
                    filledLabelColor = modalColors.secondaryText,
                    filledPlaceholderColor = modalColors.placeholderText,
                    filledSupportingColor = modalColors.mutedText,
                    filledCursorColor = ForestGlow,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = uiState.odometerError,
                )
            } else {
                FormField(
                    value = uiState.tripMeterKm,
                    onValueChange = onTripMeterChange,
                    label = stringResource(R.string.fuel_entry_trip_meter_km),
                    placeholder = stringResource(R.string.end_trip_trip_meter_placeholder),
                    filled = true,
                    isError = uiState.tripMeterError != null,
                    filledContainerColor = modalColors.fieldSurface,
                    filledTextColor = modalColors.primaryText,
                    filledLabelColor = modalColors.secondaryText,
                    filledPlaceholderColor = modalColors.placeholderText,
                    filledSupportingColor = modalColors.mutedText,
                    filledCursorColor = ForestGlow,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = uiState.tripMeterError,
                )
            }

            FormField(
                value = uiState.endLocation,
                onValueChange = onEndLocationChange,
                label = stringResource(R.string.end_trip_location_label),
                placeholder = stringResource(R.string.end_trip_location_placeholder),
                filled = true,
                filledContainerColor = modalColors.fieldSurface,
                filledTextColor = modalColors.primaryText,
                filledLabelColor = modalColors.secondaryText,
                filledPlaceholderColor = modalColors.placeholderText,
                filledSupportingColor = modalColors.mutedText,
                filledCursorColor = ForestGlow,
            )

            EndTripPrimaryButton(
                text = if (uiState.isSaving) stringResource(R.string.end_trip_ending) else stringResource(R.string.end_trip_title),
                enabled = uiState.canEnd && !uiState.isSaving,
                onClick = onEndTripClick,
            )

            Text(
                text = stringResource(R.string.fuel_entry_discard),
                style = MaterialTheme.typography.labelLarge,
                color = modalColors.secondaryText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onCancelClick)
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun EndTripHeader(
    title: String,
    subtitle: String,
    onCancelClick: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = primaryText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = secondaryText,
            )
        }
        Text(
            text = stringResource(R.string.fuel_entry_cancel),
            style = MaterialTheme.typography.labelLarge,
            color = secondaryText,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable(onClick = onCancelClick)
                .padding(top = 4.dp),
        )
    }
}

@Composable
private fun EndTripDistanceToggle(
    selectedMode: DistanceInputMode,
    onModeChange: (DistanceInputMode) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    surfaceColor: Color,
    selectedSurfaceColor: Color,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.fuel_entry_distance),
            style = MaterialTheme.typography.bodySmall,
            color = secondaryText,
            fontWeight = FontWeight.Medium,
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
        ) {
            Row(
                modifier = Modifier.padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EndTripModeOption(
                    text = stringResource(R.string.fuel_entry_trip_meter),
                    selected = selectedMode == DistanceInputMode.TRIP_METER,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    selectedSurfaceColor = selectedSurfaceColor,
                    onClick = { onModeChange(DistanceInputMode.TRIP_METER) },
                )
                EndTripModeOption(
                    text = stringResource(R.string.fuel_entry_odometer),
                    selected = selectedMode == DistanceInputMode.ODOMETER,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    selectedSurfaceColor = selectedSurfaceColor,
                    onClick = { onModeChange(DistanceInputMode.ODOMETER) },
                )
            }
        }
    }
}

@Composable
private fun EndTripModeOption(
    text: String,
    selected: Boolean,
    primaryText: Color,
    secondaryText: Color,
    selectedSurfaceColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) selectedSurfaceColor else Color.Transparent,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) primaryText else secondaryText,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EndTripPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Forest, ForestGlow),
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(gradient),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}
