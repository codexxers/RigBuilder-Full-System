package com.rigbuilder.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class SocketType {
    AM4, AM5, LGA1700, LGA1851
}

@Serializable
enum class Chipset {
    // AMD AM4
    B550, X570,
    // AMD AM5
    A620, B650, B650E, X670, X670E, B850, X870, X870E,
    // Intel LGA1700 (12th/13th/14th Gen)
    B660, B760, Z690, Z790,
    // Intel LGA1851 (Arrow Lake)
    B860, Z890
}

@Serializable
enum class CpuArchitecture {
    ZEN3, ZEN4, ZEN5,
    ALDER_LAKE, RAPTOR_LAKE, ARROW_LAKE
}

@Serializable
enum class PowerTier {
    LOW, MID, HIGH, EXTREME
}

@Serializable
enum class VrmTier {
    BASIC, MID, HIGH, PREMIUM
}

@Serializable
enum class RamGeneration {
    DDR4, DDR5
}

@Serializable
enum class FormFactor {
    ITX, MATX, ATX, EATX
}

@Serializable
enum class StorageInterface {
    M2_NVME, SATA
}

@Serializable
enum class CoolerType {
    AIR, AIO
}

enum class VrmSynergy(val label: String) {
    EXCELLENT("Excellent Synergy"),
    GOOD("Good Synergy"),
    ADEQUATE("Adequate — Usable"),
    NOT_ADVISED("Not Advised — VRM may throttle")
}

enum class ComponentCategory(val displayName: String, val stepIndex: Int) {
    CPU("CPU / Processor", 0),
    MOTHERBOARD("Motherboard", 1),
    RAM("RAM / Memory", 2),
    GPU("Graphics Card", 3),
    STORAGE("Storage", 4),
    COOLER("CPU Cooler", 5),
    CASE("Case / Chassis", 6),
    PSU("Power Supply", 7),
    FAN("Case Fans", 8)
}
