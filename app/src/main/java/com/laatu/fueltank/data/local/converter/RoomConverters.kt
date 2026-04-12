package com.laatu.fueltank.data.local.converter

import androidx.room.TypeConverter
import com.laatu.fueltank.domain.model.DistanceInputMode
import com.laatu.fueltank.domain.model.TripStatus

class RoomConverters {
    @TypeConverter
    fun fromDistanceInputMode(value: DistanceInputMode?): String? = value?.name

    @TypeConverter
    fun toDistanceInputMode(value: String?): DistanceInputMode? = value?.let(DistanceInputMode::valueOf)

    @TypeConverter
    fun fromTripStatus(value: TripStatus?): String? = value?.name

    @TypeConverter
    fun toTripStatus(value: String?): TripStatus? = value?.let(TripStatus::valueOf)
}
