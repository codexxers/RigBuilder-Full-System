package com.rigbuilder.app.data.dao

import androidx.room.*
import com.rigbuilder.app.data.entity.MotherboardEntity
import com.rigbuilder.app.model.SocketType
import kotlinx.coroutines.flow.Flow

@Dao
interface MotherboardDao {
    @Query("SELECT * FROM motherboards ORDER BY price ASC")
    fun getAll(): Flow<List<MotherboardEntity>>

    @Query("SELECT * FROM motherboards WHERE socket = :socket ORDER BY price ASC")
    fun getBySocket(socket: SocketType): Flow<List<MotherboardEntity>>

    @Query("SELECT * FROM motherboards WHERE id = :id")
    fun getById(id: Int): Flow<MotherboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(mobos: List<MotherboardEntity>)

    @Query("SELECT COUNT(*) FROM motherboards")
    fun getCount(): Int
}
