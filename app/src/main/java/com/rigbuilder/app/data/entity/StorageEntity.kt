package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rigbuilder.app.model.StorageInterface
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "storages")
data class StorageEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val type: StorageInterface,        // M2_NVME or SATA
    val capacityGb: Int,
    val readSpeedMbps: Int,
    val writeSpeedMbps: Int,
    val formFactor: String,            // "M.2 2280" or "2.5 inch"
    val capacityVariants: List<String>, // Mandatory dropdown, e.g. ["500GB","1TB","2TB"]
    val price: Double,                 // Base price (for lowest capacity)
    val imageUrls: List<String> = emptyList()
)
