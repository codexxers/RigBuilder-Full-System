package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.RamEntity
import com.rigbuilder.app.model.RamGeneration
import kotlinx.coroutines.flow.Flow

@Dao
interface RamDao {
    @Query("SELECT * FROM rams ORDER BY price ASC")
    fun getAll(): Flow<List<RamEntity>>

    @Query("SELECT * FROM rams WHERE generation = :gen ORDER BY price ASC")
    fun getByGeneration(gen: RamGeneration): Flow<List<RamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(rams: List<RamEntity>)
}
