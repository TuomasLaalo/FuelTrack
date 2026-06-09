package com.laatu.fueltank.feature.fuelentry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.FormField
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.ui.theme.Forest
import com.laatu.fueltank.ui.theme.ForestGlow
import com.laatu.fueltank.ui.theme.LocalFuelTankSpacing
import com.laatu.fueltank.ui.theme.rememberFuelTrackModalColors

@Composable
fun FuelEntryFormContent(
    uiState: AddFuelEntryUiState,
    title: String,
    subtitle: String,
    saveLabel: String,
    discardLabel: String? = null,
    deleteLabel: String? = null,
    modifier: Modifier = Modifier,
    showTripAttachment: Boolean = false,
    modalStyle: Boolean = false,
    onDistanceModeChange: (DistanceInputMode) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTripMeterChange: (String) -> Unit,
    onLitersChange: (String) -> Unit,
    onPricePerLiterChange: (String) -> Unit,
    onStationBrandChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onFullTankChange: (Boolean) -> Unit,
    onAttachToActiveTripChange: (Boolean) -> Unit = {},
    onCancelClick: (() -> Unit)? = null,
    onDiscardClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onSaveClick: () -> Unit,
) {
    val spacing = LocalFuelTankSpacing.current
    var showNoteField by rememberSaveable(uiState.form.note) { mutableStateOf(uiState.form.note.isNotBlank()) }
    val hasComputedTotal =
        uiState.form.liters.parseLocalizedDecimal()?.let { it > 0.0 } == true &&
            uiState.form.pricePerLiter.parseLocalizedDecimal()?.let { it > 0.0 } == true
    val modalColors = rememberFuelTrackModalColors(modalStyle = modalStyle)

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = if (modalStyle) RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) else RoundedCornerShape(0.dp),
        color = modalColors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.large, vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FuelEntryHeader(
                title = title,
                subtitle = subtitle,
                showCancelText = modalStyle,
                onCancelClick = onCancelClick,
                primaryText = modalColors.primaryText,
                secondaryText = modalColors.secondaryText,
            )

            uiState.form.generalError?.let { ErrorCard(message = it) }

            DistanceModeToggle(
                selectedMode = uiState.form.distanceInputMode,
                onModeChange = onDistanceModeChange,
                primaryText = modalColors.primaryText,
                secondaryText = modalColors.secondaryText,
                surfaceColor = modalColors.toggleContainer,
                selectedSurfaceColor = modalColors.toggleSelected,
            )

            if (uiState.form.distanceInputMode == DistanceInputMode.TRIP_METER) {
                FormField(
                    value = uiState.form.tripMeterKm,
                    onValueChange = onTripMeterChange,
                    label = stringResource(R.string.fuel_entry_trip_meter_km),
                    placeholder = stringResource(R.string.fuel_entry_trip_meter_placeholder),
                    modifier = Modifier.testTag(UiTestTags.FuelEntryTripMeter),
                    filled = true,
                    isError = uiState.form.tripMeterError != null,
                    filledContainerColor = modalColors.fieldSurface,
                    filledTextColor = modalColors.primaryText,
                    filledLabelColor = modalColors.secondaryText,
                    filledPlaceholderColor = modalColors.placeholderText,
                    filledSupportingColor = modalColors.mutedText,
                    filledCursorColor = ForestGlow,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = uiState.form.tripMeterError,
                )
                if (uiState.form.tripMeterError == null) {
                    uiState.baselineOdometerKm?.let { baseline ->
                        Text(
                            text = stringResource(R.string.fuel_entry_baseline_odometer, baseline),
                            style = MaterialTheme.typography.bodySmall,
                            color = modalColors.mutedText,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            } else {
                FormField(
                    value = uiState.form.odometerKm,
                    onValueChange = onOdometerChange,
                    label = stringResource(R.string.fuel_entry_odometer_km),
                    placeholder = stringResource(R.string.fuel_entry_odometer_placeholder),
                    modifier = Modifier.testTag(UiTestTags.FuelEntryOdometer),
                    filled = true,
                    isError = uiState.form.odometerError != null,
                    filledContainerColor = modalColors.fieldSurface,
                    filledTextColor = modalColors.primaryText,
                    filledLabelColor = modalColors.secondaryText,
                    filledPlaceholderColor = modalColors.placeholderText,
                    filledSupportingColor = modalColors.mutedText,
                    filledCursorColor = ForestGlow,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = uiState.form.odometerError,
                )
            }

            CompactPurchaseCard(
                modalStyle = modalStyle,
                fieldSurface = modalColors.fieldSurface,
                primaryText = modalColors.primaryText,
                secondaryText = modalColors.secondaryText,
                pricePerUnit = uiState.form.pricePerLiter,
                quantity = uiState.form.liters,
                totalPriceLabel = uiState.computedTotalPriceLabel,
                pricePerUnitError = uiState.form.pricePerLiterError,
                quantityError = uiState.form.litersError,
                hasComputedTotal = hasComputedTotal,
                onPricePerUnitChange = onPricePerLiterChange,
                onQuantityChange = onLitersChange,
            )

            FormField(
                value = uiState.form.stationBrand,
                onValueChange = onStationBrandChange,
                label = stringResource(R.string.fuel_entry_station),
                placeholder = stringResource(R.string.fuel_entry_station_placeholder),
                modifier = Modifier.testTag(UiTestTags.FuelEntryStation),
                filled = true,
                filledContainerColor = modalColors.fieldSurface,
                filledTextColor = modalColors.primaryText,
                filledLabelColor = modalColors.secondaryText,
                filledPlaceholderColor = modalColors.placeholderText,
                filledSupportingColor = modalColors.mutedText,
                filledCursorColor = ForestGlow,
            )

            InlineSwitchRow(
                modalStyle = modalStyle,
                title = stringResource(R.string.fuel_entry_full_tank_refill),
                checked = uiState.form.isFullTank,
                onCheckedChange = onFullTankChange,
                colors = modalColors,
            )

            if (showTripAttachment && uiState.activeTripId != null) {
                InlineSwitchRow(
                    modalStyle = modalStyle,
                    icon = Icons.Outlined.Route,
                    title = uiState.activeTripLabel?.let {
                        stringResource(R.string.fuel_entry_attach_active_trip_named, it)
                    } ?: stringResource(R.string.fuel_entry_attach_active_trip),
                    checked = uiState.form.attachToActiveTrip,
                    onCheckedChange = onAttachToActiveTripChange,
                    colors = modalColors,
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = modalColors.fieldSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNoteField = !showNoteField },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (uiState.form.note.isBlank()) {
                            stringResource(R.string.fuel_entry_add_note)
                        } else {
                            stringResource(R.string.fuel_entry_edit_note)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = modalColors.secondaryText,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = modalColors.secondaryText.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (showNoteField) {
                FormField(
                    value = uiState.form.note,
                    onValueChange = onNoteChange,
                    label = stringResource(R.string.fuel_entry_note),
                    singleLine = false,
                    placeholder = stringResource(R.string.fuel_entry_note_placeholder),
                    filled = true,
                    filledContainerColor = modalColors.fieldSurface,
                    filledTextColor = modalColors.primaryText,
                    filledLabelColor = modalColors.secondaryText,
                    filledPlaceholderColor = modalColors.placeholderText,
                    filledSupportingColor = modalColors.mutedText,
                    filledCursorColor = ForestGlow,
                )
            }

            GradientPrimaryButton(
                text = if (uiState.form.isSaving) stringResource(R.string.fuel_entry_saving) else saveLabel,
                enabled = !uiState.form.isSaving,
                onClick = onSaveClick,
            )

            deleteLabel?.let { label ->
                TextButton(
                    onClick = { onDeleteClick?.invoke() },
                    enabled = onDeleteClick != null && !uiState.form.isSaving,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag(UiTestTags.FuelEntryDelete),
                ) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            discardLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = modalColors.secondaryText,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(enabled = onDiscardClick != null) { onDiscardClick?.invoke() }
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FuelEntryHeader(
    title: String,
    subtitle: String,
    showCancelText: Boolean,
    onCancelClick: (() -> Unit)?,
    primaryText: Color = MaterialTheme.colorScheme.onSurface,
    secondaryText: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
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
        if (onCancelClick != null) {
            Text(
                text = if (showCancelText) {
                    stringResource(R.string.fuel_entry_cancel)
                } else {
                    stringResource(R.string.fuel_entry_close)
                },
                style = MaterialTheme.typography.labelLarge,
                color = secondaryText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .widthIn(min = 64.dp)
                    .clickable(onClick = onCancelClick)
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun DistanceModeToggle(
    selectedMode: DistanceInputMode,
    onModeChange: (DistanceInputMode) -> Unit,
    primaryText: Color = MaterialTheme.colorScheme.onSurface,
    secondaryText: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    surfaceColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    selectedSurfaceColor: Color = MaterialTheme.colorScheme.surface,
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
                SegmentedModeOption(
                    text = stringResource(R.string.fuel_entry_trip_meter),
                    selected = selectedMode == DistanceInputMode.TRIP_METER,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    selectedSurfaceColor = selectedSurfaceColor,
                    onClick = { onModeChange(DistanceInputMode.TRIP_METER) },
                )
                SegmentedModeOption(
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
private fun SegmentedModeOption(
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
private fun CompactPurchaseCard(
    modalStyle: Boolean,
    fieldSurface: Color,
    primaryText: Color,
    secondaryText: Color,
    pricePerUnit: String,
    quantity: String,
    totalPriceLabel: String,
    pricePerUnitError: String?,
    quantityError: String?,
    hasComputedTotal: Boolean,
    onPricePerUnitChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FormField(
                value = pricePerUnit,
                onValueChange = onPricePerUnitChange,
                label = stringResource(R.string.fuel_entry_price_per_unit),
                placeholder = "1.85",
                modifier = Modifier
                    .weight(1f)
                    .testTag(UiTestTags.FuelEntryPricePerUnit),
                filled = true,
                isError = pricePerUnitError != null,
                filledContainerColor = fieldSurface,
                filledTextColor = primaryText,
                filledLabelColor = secondaryText,
                filledPlaceholderColor = secondaryText.copy(alpha = 0.82f),
                filledSupportingColor = secondaryText,
                filledCursorColor = ForestGlow,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = pricePerUnitError,
            )
            FormField(
                value = quantity,
                onValueChange = onQuantityChange,
                label = stringResource(R.string.fuel_entry_quantity),
                placeholder = "42.5",
                modifier = Modifier
                    .weight(1f)
                    .testTag(UiTestTags.FuelEntryQuantity),
                filled = true,
                isError = quantityError != null,
                filledContainerColor = fieldSurface,
                filledTextColor = primaryText,
                filledLabelColor = secondaryText,
                filledPlaceholderColor = secondaryText.copy(alpha = 0.82f),
                filledSupportingColor = secondaryText,
                filledCursorColor = ForestGlow,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = quantityError,
            )
        }
        if (hasComputedTotal) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.fuel_entry_total_price),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = totalPriceLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (modalStyle) Forest else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun InlineSwitchRow(
    modalStyle: Boolean,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: com.laatu.fueltank.ui.theme.FuelTrackModalColors,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryText,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.graphicsLayer(
                scaleX = 0.76f,
                scaleY = 0.76f,
            ),
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.switchCheckedThumb,
                checkedTrackColor = colors.switchCheckedTrack,
                uncheckedThumbColor = colors.switchUncheckedThumb,
                uncheckedTrackColor = colors.switchUncheckedTrack,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun GradientPrimaryButton(
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
            .testTag(UiTestTags.FuelEntrySave)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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

@Composable
private fun ErrorCard(
    message: String,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
        ),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}
