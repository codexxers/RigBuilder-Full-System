package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "gpus")
data class GpuEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val vramGb: Int,
    val lengthMm: Int,
    val thicknessSlots: Double, // e.g. 2.5 slots
    val recommendedPsuWatts: Int,
    val tdp: Int,
    val price: Double,
    val imageUrls: List<String> = emptyList(),
    val gamingTier: Int // 1-10 scale for "Can I Run It?"
)
