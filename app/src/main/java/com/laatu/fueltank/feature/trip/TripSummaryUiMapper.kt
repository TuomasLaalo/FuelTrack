package com.laatu.fueltank.feature.trip

import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.model.FuelEntry
import com.laatu.fueltank.domain.model.Trip
import com.laatu.fueltank.domain.model.TripBreak
import com.laatu.fueltank.domain.model.TripStatus
import com.laatu.fueltank.domain.model.TripTimelineItem
import com.laatu.fueltank.domain.model.TripTimelineItemType
import com.laatu.fueltank.domain.service.CurrentOdometerService
import com.laatu.fueltank.domain.service.TripSummaryService
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TripSummaryUiMapper @Inject constructor(
    private val currentOdometerService: CurrentOdometerService,
    private val tripSummaryService: TripSummaryService,
    private val stringResourceProvider: StringResourceProvider,
) {
    fun map(
        car: Car,
        trip: Trip,
        entries: List<FuelEntry>,
        trips: List<Trip>,
        tripBreaks: List<TripBreak>,
        actionError: String?,
    ): TripSummaryUiState {
        val locale = stringResourceProvider.currentLocale()
        val linkedEntries = entries.filter { it.tripId == trip.id }.sortedByDescending { it.dateTimeEpochMillis }
        val currentOdometer = currentOdometerService.resolve(car, entries, trips)
        val liveEndOdometer = when (trip.status) {
            TripStatus.ACTIVE -> maxOf(currentOdometer.odometerKm, trip.startOdometerKm)
            TripStatus.COMPLETED -> trip.endOdometerKm ?: trip.startOdometerKm
        }
        val liveTrip = trip.copy(endOdometerKm = liveEndOdometer)
        val summary = tripSummaryService.buildSummary(
            trip = liveTrip,
            linkedEntries = linkedEntries,
            tripBreaks = tripBreaks,
        )
        val isActiveTrip = trip.status == TripStatus.ACTIVE
        val dateTimeFormatter = DateTimeFormatter
            .ofPattern(stringResourceProvider.get(R.string.trip_summary_start_datetime_format), locale)
            .withZone(ZoneId.systemDefault())
        val dateFormatter = DateTimeFormatter
            .ofPattern(stringResourceProvider.get(R.string.trip_summary_header_date_format), locale)
            .withZone(ZoneId.systemDefault())
        val timeCardDateFormatter = DateTimeFormatter
            .ofPattern(stringResourceProvider.get(R.string.trip_summary_time_card_date_format), locale)
            .withZone(ZoneId.systemDefault())
        val refuelFormatter = DateTimeFormatter
            .ofPattern(stringResourceProvider.get(R.string.trip_summary_refuel_datetime_format), locale)
            .withZone(ZoneId.systemDefault())
        val timeFormatter = DateTimeFormatter
            .ofPattern(stringResourceProvider.get(R.string.trip_summary_time_only_format), locale)
            .withZone(ZoneId.systemDefault())
        val now = System.currentTimeMillis()
        val defaultBreakTime = trip.endTimeEpochMillis ?: now
        val startInstant = Instant.ofEpochMilli(trip.startTimeEpochMillis)
        val endInstant = trip.endTimeEpochMillis?.let(Instant::ofEpochMilli)
        val startDate = startInstant.atZone(ZoneId.systemDefault()).toLocalDate()
        val endDate = endInstant?.atZone(ZoneId.systemDefault())?.toLocalDate()
        val showTimeCardDates = endDate != null && startDate != endDate
        val fuelBoughtLabel = stringResourceProvider.get(
            R.string.unit_liters_short,
            decimalFormat.format(summary.fuelBoughtMilliliters / 1000.0),
        )
        val purchaseBasedFuelPer100Km = summary.purchaseBasedFuelLitersPer100Km
        val hasTripRefuels = summary.fuelBoughtMilliliters > 0L
        val showPurchaseBasedRatio = purchaseBasedFuelPer100Km != null &&
            purchaseBasedFuelPer100Km in MIN_DISPLAYABLE_PURCHASE_RATIO..MAX_DISPLAYABLE_PURCHASE_RATIO

        return TripSummaryUiState(
            title = trip.title ?: if (isActiveTrip) {
                stringResourceProvider.get(R.string.trip_summary_active_default_title)
            } else {
                stringResourceProvider.get(R.string.trip_summary_default_title)
            },
            headerSubtitle = "",
            isActive = isActiveTrip,
            tripDateLabel = dateFormatter.format(Instant.ofEpochMilli(trip.startTimeEpochMillis)),
            routeLabel = buildRouteLabel(
                startLocation = trip.startLocation,
                endLocation = trip.endLocation,
            ),
            startDateTimeLabel = dateTimeFormatter.format(startInstant),
            endDateTimeLabel = endInstant?.let { dateTimeFormatter.format(it) }
                ?: stringResourceProvider.get(R.string.trip_time_not_ended),
            startTimeLabel = timeFormatter.format(startInstant),
            endTimeLabel = endInstant?.let { timeFormatter.format(it) }
                ?: stringResourceProvider.get(R.string.trip_time_not_ended),
            startDateLabel = timeCardDateFormatter.format(startInstant),
            endDateLabel = endInstant?.let { timeCardDateFormatter.format(it) },
            showTimeCardDates = showTimeCardDates,
            startLocationLabel = trip.startLocation
                ?: stringResourceProvider.get(R.string.trip_summary_start_location_not_set),
            distanceLabel = stringResourceProvider.get(R.string.dashboard_distance_km, summary.distanceKm),
            fuelBoughtLabel = fuelBoughtLabel,
            fuelCostLabel = stringResourceProvider.get(
                R.string.unit_euro_symbol,
                decimalFormat.format(summary.fuelCostCents / 100.0),
            ),
            tripStartInput = formatTripSummaryEditableDateTime(trip.startTimeEpochMillis),
            tripEndInput = trip.endTimeEpochMillis?.let(::formatTripSummaryEditableDateTime).orEmpty(),
            newBreakStartInput = formatTripSummaryEditableDateTime(defaultBreakTime),
            newBreakEndInput = formatTripSummaryEditableDateTime(defaultBreakTime),
            totalElapsedLabel = formatDuration(summary.totalElapsedTimeMillis),
            totalBreakLabel = formatDuration(summary.totalBreakTimeMillis),
            drivingTimeLabel = formatDuration(summary.drivingTimeMillis),
            averageSpeedIncludingBreaksLabel = summary.averageSpeedIncludingBreaksKmH?.let { speed ->
                stringResourceProvider.get(R.string.trip_time_speed_kmh, decimalFormat.format(speed))
            } ?: stringResourceProvider.get(R.string.dashboard_not_available),
            averageSpeedExcludingBreaksLabel = summary.averageSpeedExcludingBreaksKmH?.let { speed ->
                stringResourceProvider.get(R.string.trip_time_speed_kmh, decimalFormat.format(speed))
            } ?: stringResourceProvider.get(R.string.dashboard_not_available),
            drivingShare = if (summary.totalElapsedTimeMillis > 0L) {
                (summary.drivingTimeMillis.toFloat() / summary.totalElapsedTimeMillis.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            },
            hasActiveBreak = tripBreaks.any { it.endTimeEpochMillis == null },
            actionError = actionError,
            overviewFuelMetricValue = when {
                showPurchaseBasedRatio -> stringResourceProvider.get(
                    R.string.trip_summary_purchase_based_fuel_value,
                    wholeNumberFormat.format(purchaseBasedFuelPer100Km),
                    stringResourceProvider.get(R.string.dashboard_consumption_unit),
                )
                hasTripRefuels -> fuelBoughtLabel
                else -> stringResourceProvider.get(R.string.trip_summary_no_trip_refuels_short)
            },
            overviewFuelMetricLabel = if (showPurchaseBasedRatio) {
                stringResourceProvider.get(R.string.trip_summary_metric_bought_per_100km)
            } else {
                stringResourceProvider.get(R.string.trip_summary_metric_fuel_bought)
            },
            overviewFuelMetricNote = when {
                showPurchaseBasedRatio -> stringResourceProvider.get(R.string.trip_summary_purchase_based_note)
                hasTripRefuels -> stringResourceProvider.get(R.string.trip_summary_purchase_ratio_hidden_note)
                else -> stringResourceProvider.get(R.string.trip_summary_no_trip_refuels_note)
            },
            breaks = tripBreaks.map { tripBreak ->
                val start = timeFormatter.format(Instant.ofEpochMilli(tripBreak.startTimeEpochMillis))
                val end = tripBreak.endTimeEpochMillis?.let { timeFormatter.format(Instant.ofEpochMilli(it)) }
                    ?: stringResourceProvider.get(R.string.trip_time_break_active)
                val durationEnd = tripBreak.endTimeEpochMillis ?: now
                TripBreakUiModel(
                    id = tripBreak.id,
                    timeRangeLabel = stringResourceProvider.get(R.string.trip_time_range, start, end),
                    durationLabel = formatDuration(
                        (durationEnd - tripBreak.startTimeEpochMillis).coerceAtLeast(0L),
                    ),
                    note = tripBreak.note.orEmpty(),
                    startInput = formatTripSummaryEditableDateTime(tripBreak.startTimeEpochMillis),
                    endInput = tripBreak.endTimeEpochMillis?.let(::formatTripSummaryEditableDateTime).orEmpty(),
                )
            },
            timeline = summary.timeline.map { item ->
                item.toUiItem(timeFormatter = timeFormatter)
            },
            linkedRefuels = linkedEntries.map { entry ->
                TripLinkedRefuelUiModel(
                    id = entry.id,
                    stationLabel = entry.stationBrand?.ifBlank { null }
                        ?: stringResourceProvider.get(R.string.trip_summary_station_not_set),
                    dateTimeLabel = refuelFormatter.format(Instant.ofEpochMilli(entry.dateTimeEpochMillis)),
                    litersLabel = stringResourceProvider.get(
                        R.string.unit_liters_short,
                        decimalFormat.format(entry.litersMilliliters / 1000.0),
                    ),
                    costLabel = stringResourceProvider.get(
                        R.string.unit_euro_symbol,
                        decimalFormat.format(entry.totalCostCents / 100.0),
                    ),
                )
            },
        )
    }

    private fun buildRouteLabel(
        startLocation: String?,
        endLocation: String?,
    ): String {
        val start = startLocation?.ifBlank { null }
            ?: stringResourceProvider.get(R.string.trip_summary_start_location_not_set)
        val end = endLocation?.ifBlank { null }
            ?: stringResourceProvider.get(R.string.trip_summary_end_location_not_set)
        return stringResourceProvider.get(R.string.trip_summary_route_separator, start, end)
    }

    private fun TripTimelineItem.toUiItem(
        timeFormatter: DateTimeFormatter,
    ): TripTimelineUiItem {
        val start = timeFormatter.format(Instant.ofEpochMilli(startTimeEpochMillis))
        return when (type) {
            TripTimelineItemType.TRIP_STARTED -> TripTimelineUiItem(
                id = id,
                type = TripTimelineUiType.TRIP_STARTED,
                title = stringResourceProvider.get(R.string.trip_timeline_trip_started),
                primaryLabel = "",
                secondaryLabel = "",
                trailingLabel = start,
            )
            TripTimelineItemType.TRIP_ENDED -> TripTimelineUiItem(
                id = id,
                type = TripTimelineUiType.TRIP_ENDED,
                title = stringResourceProvider.get(R.string.trip_timeline_trip_ended),
                primaryLabel = "",
                secondaryLabel = "",
                trailingLabel = start,
            )
            TripTimelineItemType.BREAK -> {
                val end = endTimeEpochMillis?.let { timeFormatter.format(Instant.ofEpochMilli(it)) }
                    ?: stringResourceProvider.get(R.string.trip_time_break_active)
                TripTimelineUiItem(
                    id = id,
                    type = TripTimelineUiType.BREAK,
                    title = note?.ifBlank { null } ?: stringResourceProvider.get(R.string.trip_timeline_break),
                    primaryLabel = stringResourceProvider.get(R.string.trip_time_range, start, end),
                    secondaryLabel = formatDuration(durationMillis),
                    trailingLabel = start,
                    editableBreakId = id.removePrefix("break-").toLongOrNull(),
                )
            }
            TripTimelineItemType.FUEL_ENTRY -> TripTimelineUiItem(
                id = id,
                type = TripTimelineUiType.FUEL_ENTRY,
                title = stringResourceProvider.get(R.string.trip_timeline_fuel_entry),
                primaryLabel = stringResourceProvider.get(
                    R.string.trip_timeline_fuel_entry_summary,
                    decimalFormat.format((fuelLitersMilliliters ?: 0L) / 1000.0),
                    decimalFormat.format((fuelCostCents ?: 0L) / 100.0),
                ),
                secondaryLabel = "",
                trailingLabel = start,
            )
        }
    }

    private fun formatDuration(durationMillis: Long): String {
        val duration = Duration.ofMillis(durationMillis.coerceAtLeast(0L))
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return when {
            hours > 0 -> stringResourceProvider.get(R.string.trip_summary_duration_hours_minutes, hours, minutes)
            else -> stringResourceProvider.get(R.string.trip_summary_duration_minutes, minutes)
        }
    }
}

internal fun formatTripSummaryEditableDateTime(epochMillis: Long): String {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
        .format(tripSummaryEditDateTimeFormatter)
}

internal val tripSummaryEditDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private val decimalFormat = DecimalFormat("0.0#")
private val wholeNumberFormat = DecimalFormat("0")
private const val MIN_DISPLAYABLE_PURCHASE_RATIO = 2.0
private const val MAX_DISPLAYABLE_PURCHASE_RATIO = 25.0
