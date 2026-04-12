package com.laatu.fueltank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.laatu.fueltank.data.local.entity.CarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY id LIMIT 1")
    fun observePrimaryCar(): Flow<CarEntity?>

    @Query("SELECT * FROM cars ORDER BY id LIMIT 1")
    suspend fun getPrimaryCar(): CarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCar(car: CarEntity): Long
}
