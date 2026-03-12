package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rigbuilder.app.model.FormFactor
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val supportedFormFactors: List<FormFactor>,
    val maxGpuLengthMm: Int,
    val maxCpuCoolerHeightMm: Int,
    val maxPsuLengthMm: Int,
    val maxRadiatorSizeMm: Int,     // Largest radiator supported (0 = none)
    val totalFanSlots: Int,         // Total number of fan mounting points
    val includedFans: Int,          // Pre-installed fans
    val colorVariants: List<String>, // Mandatory dropdown
    val price: Double,
    val imageUrls: List<String> = emptyList()
)
