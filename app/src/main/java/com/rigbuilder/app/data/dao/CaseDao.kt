package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.CaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY price ASC")
    fun getAll(): Flow<List<CaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<CaseEntity>)
}
