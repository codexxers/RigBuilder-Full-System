package com.rigbuilder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "psus")
data class PsuEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val wattage: Int,
    val efficiency: String,     // e.g. "80+ Gold", "80+ Bronze"
    val modularity: String,     // "Full", "Semi", "Non-Modular"
    val lengthMm: Int,          // For case clearance check
    val price: Double,
    val imageUrls: List<String> = emptyList()
)
