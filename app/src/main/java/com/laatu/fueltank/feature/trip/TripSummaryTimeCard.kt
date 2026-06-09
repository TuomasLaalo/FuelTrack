package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags

@Composable
fun TripTimeDistributionCard(
    uiState: TripSummaryUiState,
    onStartBreakClick: () -> Unit,
    onEndBreakClick: () -> Unit,
    onAddBreakClick: () -> Unit,
    onEditTripTimesClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.actionError != null) {
                Text(
                    text = uiState.actionError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TripTimePill(
                    label = stringResource(R.string.trip_time_start_time),
                    time = uiState.startTimeLabel,
                    date = uiState.startDateLabel.takeIf { uiState.showTimeCardDates },
                    modifier = Modifier.weight(1f),
                )
                TripTimePill(
                    label = stringResource(R.string.trip_time_end_time),
                    time = uiState.endTimeLabel,
                    date = uiState.endDateLabel.takeIf { uiState.showTimeCardDates },
                    modifier = Modifier.weight(1f),
                )
            }
            TripTimeSplitBar(
                drivingShare = uiState.drivingShare,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TripTimeLegendItem(
                    color = TripTimeDrivingColor,
                    text = stringResource(R.string.trip_time_driving_legend, uiState.drivingTimeLabel),
                )
                TripTimeLegendItem(
                    color = TripTimeBreakColor,
                    text = stringResource(R.string.trip_time_breaks_legend, uiState.totalBreakLabel),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (uiState.isActive) {
                    val breakButtonContainer = if (uiState.hasActiveBreak) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        TripTimeBreakColor.copy(alpha = 0.64f)
                    }
                    val breakButtonContent = if (uiState.hasActiveBreak) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    FilledTonalButton(
                        onClick = if (uiState.hasActiveBreak) onEndBreakClick else onStartBreakClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(
                                if (uiState.hasActiveBreak) {
                                    UiTestTags.TripTimeEndBreak
                                } else {
                                    UiTestTags.TripTimeStartBreak
                                },
                            ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = breakButtonContainer,
                            contentColor = breakButtonContent,
                        ),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = if (uiState.hasActiveBreak) {
                                    Icons.Outlined.PlayCircle
                                } else {
                                    Icons.Outlined.PauseCircle
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (uiState.hasActiveBreak) {
                                    stringResource(R.string.trip_time_end_break)
                                } else {
                                    stringResource(R.string.trip_time_start_break)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onAddBreakClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.trip_time_add_break),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                TextButton(
                    onClick = onEditTripTimesClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.trip_time_edit_times_short),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TripTimePill(
    label: String,
    time: String,
    date: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            date?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun TripTimeSplitBar(
    drivingShare: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = TripTimeBreakColor,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(0.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(drivingShare.coerceAtLeast(0.01f))
                .height(8.dp)
                .background(
                    color = TripTimeDrivingColor,
                    shape = RoundedCornerShape(999.dp),
                ),
        )
        Box(
            modifier = Modifier
                .weight((1f - drivingShare).coerceAtLeast(0.01f))
                .height(8.dp),
        )
    }
}

@Composable
private fun TripTimeLegendItem(
    color: Color,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp)),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal val TripTimeBreakColor = Color(0xFFF3C6D2)
internal val TripTimeDrivingColor = Color(0xFF007A5A)
