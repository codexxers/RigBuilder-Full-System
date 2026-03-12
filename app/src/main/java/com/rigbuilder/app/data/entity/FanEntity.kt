package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "fans")
data class FanEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val sizeMm: Int,            // 120 or 140
    val quantity: Int,          // 1 = single, 3 = 3-in-1 pack
    val airflowCfm: Double,
    val noiseLevelDba: Double,
    val rgbType: String?,       // null, "ARGB", "RGB"
    val price: Double,          // Price for the pack
    val imageUrls: List<String> = emptyList()
)
