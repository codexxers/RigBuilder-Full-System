package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.PsuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PsuDao {
    @Query("SELECT * FROM psus ORDER BY price ASC")
    fun getAll(): Flow<List<PsuEntity>>

    @Query("SELECT * FROM psus WHERE wattage >= :minWatts ORDER BY wattage ASC")
    fun getByMinWattage(minWatts: Int): Flow<List<PsuEntity>>

    @Query("SELECT * FROM psus WHERE wattage >= :minWatts AND lengthMm <= :maxLen ORDER BY wattage ASC")
    fun getByWattageAndLength(minWatts: Int, maxLen: Int): Flow<List<PsuEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<PsuEntity>)
}
