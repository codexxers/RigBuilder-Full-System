package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rigbuilder.app.model.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "rams")
data class RamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val generation: RamGeneration,
    val capacityGb: Int,
    val speedMhz: Int,
    val latency: String, // e.g. "CL16"
    val modules: Int,    // e.g. 2 for 2x8GB
    val colorVariants: List<String>, // Mandatory dropdown
    val price: Double,
    val imageUrls: List<String> = emptyList()
)
