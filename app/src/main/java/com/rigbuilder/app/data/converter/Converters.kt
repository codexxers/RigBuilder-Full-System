package com.rigbuilder.app.data.converter

import androidx.room.TypeConverter
import com.rigbuilder.app.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    // --- String Lists ---
    @TypeConverter fun fromStringList(value: List<String>): String = json.encodeToString(value)
    @TypeConverter fun toStringList(value: String): List<String> = json.decodeFromString(value)

    // --- Chipset Lists ---
    @TypeConverter fun fromChipsetList(value: List<Chipset>): String = json.encodeToString(value)
    @TypeConverter fun toChipsetList(value: String): List<Chipset> = json.decodeFromString(value)

    // --- SocketType Lists ---
    @TypeConverter fun fromSocketTypeList(value: List<SocketType>): String = json.encodeToString(value)
    @TypeConverter fun toSocketTypeList(value: String): List<SocketType> = json.decodeFromString(value)

    // --- FormFactor Lists ---
    @TypeConverter fun fromFormFactorList(value: List<FormFactor>): String = json.encodeToString(value)
    @TypeConverter fun toFormFactorList(value: String): List<FormFactor> = json.decodeFromString(value)

    // --- Single Enums ---
    @TypeConverter fun fromSocketType(v: SocketType): String = v.name
    @TypeConverter fun toSocketType(v: String): SocketType = SocketType.valueOf(v)

    @TypeConverter fun fromChipset(v: Chipset): String = v.name
    @TypeConverter fun toChipset(v: String): Chipset = Chipset.valueOf(v)

    @TypeConverter fun fromCpuArch(v: CpuArchitecture): String = v.name
    @TypeConverter fun toCpuArch(v: String): CpuArchitecture = CpuArchitecture.valueOf(v)

    @TypeConverter fun fromPowerTier(v: PowerTier): String = v.name
    @TypeConverter fun toPowerTier(v: String): PowerTier = PowerTier.valueOf(v)

    @TypeConverter fun fromVrmTier(v: VrmTier): String = v.name
    @TypeConverter fun toVrmTier(v: String): VrmTier = VrmTier.valueOf(v)

    @TypeConverter fun fromRamGen(v: RamGeneration): String = v.name
    @TypeConverter fun toRamGen(v: String): RamGeneration = RamGeneration.valueOf(v)

    @TypeConverter fun fromFormFactor(v: FormFactor): String = v.name
    @TypeConverter fun toFormFactor(v: String): FormFactor = FormFactor.valueOf(v)

    @TypeConverter fun fromStorageIf(v: StorageInterface): String = v.name
    @TypeConverter fun toStorageIf(v: String): StorageInterface = StorageInterface.valueOf(v)

    @TypeConverter fun fromCoolerType(v: CoolerType): String = v.name
    @TypeConverter fun toCoolerType(v: String): CoolerType = CoolerType.valueOf(v)
}
