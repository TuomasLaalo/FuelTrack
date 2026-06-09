package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.CompactSectionHeader

@Composable
fun TripTimelineCard(
    items: List<TripTimelineUiItem>,
    breaks: List<TripBreakUiModel>,
    onEditBreakClick: (TripBreakUiModel) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompactSectionHeader(title = stringResource(R.string.trip_timeline_title))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items.forEachIndexed { index, item ->
                    val breakItem = item.editableBreakId?.let { breakId ->
                        breaks.firstOrNull { it.id == breakId }
                    }
                    TimelineRow(
                        item = item,
                        isFirst = index == 0,
                        isLast = index == items.lastIndex,
                        onClick = breakItem?.let { { onEditBreakClick(it) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    item: TripTimelineUiItem,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: (() -> Unit)?,
) {
    val rowShape = RoundedCornerShape(14.dp)
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = rowShape,
        color = Color.Transparent,
        modifier = if (item.type == TripTimelineUiType.BREAK) {
            Modifier.testTag(UiTestTags.TripTimelineBreak)
        } else {
            Modifier
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            TimelineMarker(
                item = item,
                isFirst = isFirst,
                isLast = isLast,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = item.titleWithDuration(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (item.type.isEndpoint) FontWeight.Bold else FontWeight.SemiBold,
                )
                if (item.primaryLabel.isNotBlank()) {
                    Text(
                        text = item.primaryLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.trailingLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.common_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineMarker(
    item: TripTimelineUiItem,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val dotColor = when (item.type) {
        TripTimelineUiType.TRIP_STARTED -> TripTimeDrivingColor
        TripTimelineUiType.TRIP_ENDED -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
    }
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
    val innerDotColor = MaterialTheme.colorScheme.surfaceContainerLow

    Canvas(
        modifier = Modifier.size(width = 18.dp, height = 52.dp),
    ) {
        val centerX = size.width / 2f
        val centerY = 13.dp.toPx()
        if (!isFirst) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, centerY - 8.dp.toPx()),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, centerY + 8.dp.toPx()),
                end = Offset(centerX, size.height),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        drawCircle(
            color = dotColor,
            radius = if (item.type.isEndpoint) 6.dp.toPx() else 5.dp.toPx(),
            center = Offset(centerX, centerY),
        )
        if (!item.type.isEndpoint) {
            drawCircle(
                color = innerDotColor,
                radius = 3.2.dp.toPx(),
                center = Offset(centerX, centerY),
            )
        }
    }
}

@Composable
private fun TripTimelineUiItem.titleWithDuration(): String {
    return if (secondaryLabel.isBlank()) {
        title
    } else {
        stringResource(R.string.trip_timeline_title_with_duration, title, secondaryLabel)
    }
}

private val TripTimelineUiType.isEndpoint: Boolean
    get() = this == TripTimelineUiType.TRIP_STARTED || this == TripTimelineUiType.TRIP_ENDED
