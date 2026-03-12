package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.CoolerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoolerDao {
    @Query("SELECT * FROM coolers ORDER BY price ASC")
    fun getAll(): Flow<List<CoolerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<CoolerEntity>)
}
