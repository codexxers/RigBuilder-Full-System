package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val minCpuTier: Int,    // Minimum gaming tier (1-10) for 30fps low
    val recCpuTier: Int,    // Recommended gaming tier for 60fps high
    val minGpuTier: Int,
    val recGpuTier: Int,
    val minRamGb: Int,
    val recRamGb: Int,
    val minStorageGb: Int,
    val imageUrl: String = ""
)
