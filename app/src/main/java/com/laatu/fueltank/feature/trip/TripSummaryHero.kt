package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R

@Composable
fun TripRouteHeroCard(uiState: TripSummaryUiState) {
    val routeLine = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.32f)
    val startDot = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
    val dotHalo = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
    val endDot = Color(0xFFF5A45C)
    val chipColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                ) {
                    val centerY = size.height / 2f
                    val startX = 6.dp.toPx()
                    val endX = size.width - 6.dp.toPx()
                    drawLine(
                        color = routeLine,
                        start = Offset(startX, centerY),
                        end = Offset(endX, centerY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 6.dp.toPx())),
                    )
                    drawCircle(
                        color = dotHalo,
                        radius = 5.dp.toPx(),
                        center = Offset(startX, centerY),
                    )
                    drawCircle(
                        color = startDot,
                        radius = 3.2.dp.toPx(),
                        center = Offset(startX, centerY),
                    )
                    drawCircle(
                        color = dotHalo,
                        radius = 5.dp.toPx(),
                        center = Offset(endX, centerY),
                    )
                    drawCircle(
                        color = endDot,
                        radius = 3.2.dp.toPx(),
                        center = Offset(endX, centerY),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                ) {
                    Text(
                        text = uiState.distanceLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = uiState.startLocationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = uiState.routeLabel.substringAfter(" - ", uiState.routeLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TripHeroStatChip(
                    value = uiState.totalElapsedLabel,
                    label = stringResource(R.string.trip_time_elapsed),
                    containerColor = chipColor,
                    modifier = Modifier.weight(1f),
                )
                TripHeroStatChip(
                    value = uiState.drivingTimeLabel,
                    label = stringResource(R.string.trip_time_driving),
                    containerColor = chipColor,
                    modifier = Modifier.weight(1f),
                )
                TripHeroStatChip(
                    value = uiState.overviewFuelMetricValue,
                    label = uiState.overviewFuelMetricLabel,
                    containerColor = chipColor,
                    modifier = Modifier.weight(1f),
                )
            }
            if (uiState.overviewFuelMetricNote.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(16.dp),
                    )
                    Text(
                        text = uiState.overviewFuelMetricNote,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.74f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TripHeroStatChip(
    value: String,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.68f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
