package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rigbuilder.app.model.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cpus")
data class CpuEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val socket: SocketType,
    val architecture: CpuArchitecture,
    val powerTier: PowerTier,
    val supportedChipsets: List<Chipset>,
    val cores: Int,
    val threads: Int,
    val baseClockGhz: Double,
    val boostClockGhz: Double,
    val tdp: Int,
    val integratedGraphics: String? = null,
    val price: Double,
    val imageUrls: List<String> = emptyList(),
    val gamingTier: Int // 1-10 scale for "Can I Run It?"
)
