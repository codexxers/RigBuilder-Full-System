package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rigbuilder.app.model.CoolerType
import com.rigbuilder.app.model.SocketType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "coolers")
data class CoolerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val type: CoolerType,                 // AIR or AIO
    val supportedSockets: List<SocketType>,
    val heightMm: Int,                    // Tower height (air) or 0 (AIO)
    val radiatorSizeMm: Int,              // 0 (air) or 120/240/280/360 (AIO)
    val fanSizeMm: Int,                   // 120 or 140mm fans
    val tdpRating: Int,                   // Max TDP it can handle
    val price: Double,
    val imageUrls: List<String> = emptyList()
)
