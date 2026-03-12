package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rigbuilder.app.model.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "motherboards")
data class MotherboardEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val socket: SocketType,
    val chipset: Chipset,
    val formFactor: FormFactor,
    val ramGeneration: RamGeneration,
    val ramSlots: Int,
    val maxRamSpeedMhz: Int,
    val m2Slots: Int,
    val sataSlots: Int,
    val vrmTier: VrmTier,
    val price: Double,
    val imageUrls: List<String> = emptyList()
)
