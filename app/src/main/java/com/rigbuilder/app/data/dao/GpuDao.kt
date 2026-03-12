package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.GpuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpuDao {
    @Query("SELECT * FROM gpus ORDER BY price ASC")
    fun getAll(): Flow<List<GpuEntity>>

    @Query("SELECT * FROM gpus WHERE id = :id")
    fun getById(id: Int): Flow<GpuEntity?>

    @Query("SELECT COUNT(*) FROM gpus")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(gpus: List<GpuEntity>)
}
