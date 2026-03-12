package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.StorageEntity
import com.rigbuilder.app.model.StorageInterface
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {
    @Query("SELECT * FROM storages ORDER BY price ASC")
    fun getAll(): Flow<List<StorageEntity>>

    @Query("SELECT * FROM storages WHERE type = :type ORDER BY price ASC")
    fun getByType(type: StorageInterface): Flow<List<StorageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<StorageEntity>)
}
