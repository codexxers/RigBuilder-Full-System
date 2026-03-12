package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY name ASC")
    fun getAll(): Flow<List<GameEntity>>

    @Query("SELECT COUNT(*) FROM games")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<GameEntity>)
}
