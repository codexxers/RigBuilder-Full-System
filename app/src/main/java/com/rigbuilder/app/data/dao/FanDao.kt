package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.FanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FanDao {
    @Query("SELECT * FROM fans ORDER BY price ASC")
    fun getAll(): Flow<List<FanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<FanEntity>)
}
