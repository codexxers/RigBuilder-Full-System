package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.CpuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CpuDao {
    @Query("SELECT * FROM cpus ORDER BY price ASC")
    fun getAll(): Flow<List<CpuEntity>>

    @Query("SELECT * FROM cpus WHERE id = :id")
    fun getById(id: Int): Flow<CpuEntity?>

    @Query("SELECT COUNT(*) FROM cpus")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cpus: List<CpuEntity>)
}
