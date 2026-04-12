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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.core.ui.component.ConfirmationDialog
import com.laatu.fueltank.core.ui.component.FormField
import com.laatu.fueltank.ui.theme.Forest
import com.laatu.fueltank.ui.theme.ForestGlow
import com.laatu.fueltank.ui.theme.LocalFuelTankSpacing

@Composable
fun StartTripRoute(
    onNavigateBack: () -> Unit,
    onTripStarted: (Long) -> Unit,
    viewModel: StartTripViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val isDirty = uiState.title.isNotBlank() || uiState.startLocation.isNotBlank()

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
            if (event is StartTripEvent.TripStarted) {
                onTripStarted(event.tripId)
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
                .heightIn(min = 520.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ),
        ) {
            StartTripSheet(
                uiState = uiState,
                modifier = Modifier.padding(top = 12.dp),
                onTitleChange = viewModel::onTitleChange,
                onStartLocationChange = viewModel::onStartLocationChange,
                onCancelClick = ::requestClose,
                onStartClick = viewModel::submit,
            )
        }
    }

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = "Discard trip draft?",
            message = "Your current trip details will be lost.",
            confirmLabel = "Discard",
            dismissLabel = "Keep editing",
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
}

@Composable
private fun StartTripSheet(
    uiState: StartTripUiState,
    modifier: Modifier = Modifier,
    onTitleChange: (String) -> Unit,
    onStartLocationChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    val spacing = LocalFuelTankSpacing.current
    val modalBackgroundColor = MaterialTheme.colorScheme.background
    val modalSurfaceColorAlt = Color(0xFFE2E6E0)
    val modalPrimaryText = Color(0xFF17352D)
    val modalSecondaryText = Color(0xFF305A4E)
    val modalMutedText = Color(0xFF3E6D60)
    val modalFieldPlaceholder = Color(0xFF64736F)

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = modalBackgroundColor,
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.large, vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StartTripHeader(
                title = "Start Trip",
                subtitle = "Capture your trip details and start mileage",
                onCancelClick = onCancelClick,
                primaryText = modalPrimaryText,
                secondaryText = modalSecondaryText,
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = modalSurfaceColorAlt,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Trip start point",
                        style = MaterialTheme.typography.labelLarge,
                        color = modalSecondaryText,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Starting odometer: ${uiState.currentOdometerLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = modalPrimaryText,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            FormField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "Trip title",
                placeholder = "Weekend drive, commute, cabin trip...",
                filled = true,
                filledContainerColor = modalSurfaceColorAlt,
                filledTextColor = modalPrimaryText,
                filledLabelColor = modalSecondaryText,
                filledPlaceholderColor = modalFieldPlaceholder,
                filledSupportingColor = modalMutedText,
                filledCursorColor = ForestGlow,
            )

            FormField(
                value = uiState.startLocation,
                onValueChange = onStartLocationChange,
                label = "Start location",
                placeholder = "Helsinki, Tampere, home...",
                filled = true,
                filledContainerColor = modalSurfaceColorAlt,
                filledTextColor = modalPrimaryText,
                filledLabelColor = modalSecondaryText,
                filledPlaceholderColor = modalFieldPlaceholder,
                filledSupportingColor = modalMutedText,
                filledCursorColor = ForestGlow,
            )

            StartTripPrimaryButton(
                text = if (uiState.isSaving) "Starting..." else "Start Trip",
                enabled = uiState.canStart && !uiState.isSaving,
                onClick = onStartClick,
            )

            Text(
                text = "Discard",
                style = MaterialTheme.typography.labelLarge,
                color = modalSecondaryText,
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
private fun StartTripHeader(
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
            text = "Cancel",
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
private fun StartTripPrimaryButton(
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
