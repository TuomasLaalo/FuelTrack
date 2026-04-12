package com.laatu.fueltank.feature.carsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laatu.fueltank.core.ui.component.AppScaffold
import com.laatu.fueltank.core.ui.component.FormField
import com.laatu.fueltank.ui.theme.Forest
import com.laatu.fueltank.ui.theme.ForestGlow
import com.laatu.fueltank.ui.theme.LocalFuelTankSpacing

@Composable
fun CarSetupRoute(
    onCarCreated: () -> Unit,
    viewModel: CarSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is CarSetupEvent.CarSaved) {
                onCarCreated()
            }
        }
    }

    AppScaffold(
        title = "",
        showTopBar = false,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        CarSetupScreen(
            padding = padding,
            uiState = uiState,
            onNameChange = viewModel::onNameChange,
            onTankSizeChange = viewModel::onTankSizeChange,
            onInitialOdometerChange = viewModel::onInitialOdometerChange,
            onSaveClick = viewModel::submit,
        )
    }
}

@Composable
private fun CarSetupScreen(
    padding: PaddingValues,
    uiState: CarSetupUiState,
    onNameChange: (String) -> Unit,
    onTankSizeChange: (String) -> Unit,
    onInitialOdometerChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    val spacing = LocalFuelTankSpacing.current
    val fieldSurface = MaterialTheme.colorScheme.surfaceContainerLowest

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
            .padding(horizontal = spacing.large)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        CarSetupHeader()

        CarSetupIntroCard()

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Vehicle details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )

                FormField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = "Car name",
                    placeholder = "Golf, Corolla, A4...",
                    filled = true,
                    isError = uiState.nameError != null,
                    filledContainerColor = fieldSurface,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    supportingText = uiState.nameError,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FormField(
                        value = uiState.tankSizeLiters,
                        onValueChange = onTankSizeChange,
                        label = "Tank size (L)",
                        placeholder = "55",
                        modifier = Modifier.weight(1f),
                        filled = true,
                        isError = uiState.tankSizeError != null,
                        filledContainerColor = fieldSurface,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = uiState.tankSizeError,
                    )
                    FormField(
                        value = uiState.initialOdometerKm,
                        onValueChange = onInitialOdometerChange,
                        label = "Odometer (km)",
                        placeholder = "128500",
                        modifier = Modifier.weight(1f),
                        filled = true,
                        isError = uiState.odometerError != null,
                        filledContainerColor = fieldSurface,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = uiState.odometerError,
                    )
                }
            }
        }

        CarSetupPrimaryButton(
            text = if (uiState.isSaving) "Saving..." else "Save car",
            isSaving = uiState.isSaving,
            onClick = onSaveClick,
        )

        Box(modifier = Modifier.padding(bottom = spacing.xxLarge))
    }
}

@Composable
private fun CarSetupHeader() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Create Your Car",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Set up the car before logging fuel entries or trips.",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CarSetupIntroCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Getting started",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "FuelTrack uses your car details as the baseline for fuel history, trip tracking, and odometer normalization.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CarSetupPrimaryButton(
    text: String,
    isSaving: Boolean,
    onClick: () -> Unit,
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Forest, ForestGlow),
    )
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(gradient),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.padding(end = 2.dp),
                )
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
