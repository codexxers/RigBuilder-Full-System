package com.rigbuilder.app.model

data class PreBuiltData(
    val id: String,
    val name: String,
    val tier: String,
    val price: Int,
    val cpu: String,
    val gpu: String,
    val ram: String,
    val storage: String,
    val psu: String,
    val case_: String,
    // Internal-only — used for Can I Run It / System Quality calculations, never displayed
    val cpuTier: Int,
    val gpuTier: Int,
    val vrmTier: Int,
    val psuTier: Int
)
