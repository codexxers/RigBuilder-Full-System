package com.rigbuilder.app.data.repository

import com.rigbuilder.app.data.database.AppDatabase
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Wrapper holding a motherboard and its VRM synergy assessment relative to the selected CPU. */
data class MotherboardWithSynergy(
    val motherboard: MotherboardEntity,
    val synergy: VrmSynergy,
    val warning: String?
)

/** Wrapper for cases with clearance compatibility info. */
data class CaseCompatibility(
    val case: CaseEntity,
    val gpuFits: Boolean,
    val coolerFits: Boolean,
    val formFactorFits: Boolean,
    val isFullyCompatible: Boolean
)

/** Result for the bottleneck calculator. */
data class BottleneckResult(
    val synergyPercentage: Int,        // 0-100
    val bottleneck: String,            // "CPU", "GPU", or "Balanced"
    val advice: String
)

class ComponentRepository(private val db: AppDatabase) {

    // ── CPU ──────────────────────────────────────────────────────
    fun getAllCpus(): Flow<List<CpuEntity>> = db.cpuDao().getAll()

    // ── Motherboard (filtered by CPU) ───────────────────────────
    fun getFilteredMotherboards(cpu: CpuEntity): Flow<List<MotherboardWithSynergy>> {
        return db.motherboardDao().getBySocket(cpu.socket).map { motherboards ->
            motherboards
                .filter { it.chipset in cpu.supportedChipsets }
                .map { mobo ->
                    val synergy = calculateVrmSynergy(cpu.powerTier, mobo.vrmTier)
                    MotherboardWithSynergy(
                        motherboard = mobo,
                        synergy = synergy,
                        warning = when (synergy) {
                            VrmSynergy.NOT_ADVISED ->
                                "⚠ This motherboard's VRM is not recommended for the ${cpu.name}. " +
                                "Consider a board with better power delivery to avoid throttling."
                            VrmSynergy.ADEQUATE ->
                                "This board will work but may limit overclocking headroom."
                            else -> null
                        }
                    )
                }
                .sortedWith(compareBy({ it.synergy.ordinal }, { it.motherboard.price }))
        }
    }

    // ── RAM (filtered by Motherboard generation) ────────────────
    fun getFilteredRam(mobo: MotherboardEntity): Flow<List<RamEntity>> {
        return db.ramDao().getByGeneration(mobo.ramGeneration)
    }

    // ── GPU ──────────────────────────────────────────────────────
    fun getAllGpus(): Flow<List<GpuEntity>> = db.gpuDao().getAll()

    // ── Storage ─────────────────────────────────────────────────
    fun getAllStorages(): Flow<List<StorageEntity>> = db.storageDao().getAll()

    // ── Cooler (filtered by CPU socket) ─────────────────────────
    fun getFilteredCoolers(cpuSocket: SocketType): Flow<List<CoolerEntity>> {
        return db.coolerDao().getAll().map { coolers ->
            coolers.filter { cpuSocket in it.supportedSockets }
        }
    }

    // ── Case (filtered by form factor + clearances) ─────────────
    fun getFilteredCases(
        moboFormFactor: FormFactor,
        gpuLengthMm: Int,
        coolerHeightMm: Int,
        coolerRadiatorMm: Int
    ): Flow<List<CaseCompatibility>> {
        return db.caseDao().getAll().map { cases ->
            cases.map { case ->
                val ffFits = moboFormFactor in case.supportedFormFactors
                val gpuFits = gpuLengthMm <= case.maxGpuLengthMm
                val coolerFits = if (coolerRadiatorMm > 0) {
                    coolerRadiatorMm <= case.maxRadiatorSizeMm
                } else {
                    coolerHeightMm <= case.maxCpuCoolerHeightMm
                }
                CaseCompatibility(
                    case = case,
                    gpuFits = gpuFits,
                    coolerFits = coolerFits,
                    formFactorFits = ffFits,
                    isFullyCompatible = ffFits && gpuFits && coolerFits
                )
            }.sortedByDescending { it.isFullyCompatible }
        }
    }

    // ── PSU ──────────────────────────────────────────────────────
    /** Returns ALL PSUs that physically fit the case — wattage warnings are handled by the UI. */
    fun getAllPsusFilteredByLength(caseMaxPsuLengthMm: Int): Flow<List<PsuEntity>> {
        return db.psuDao().getAll().map { psus ->
            psus.filter { it.lengthMm <= caseMaxPsuLengthMm }
        }
    }

    /** Calculate total system TDP from selected components. */
    fun calculateTotalTdp(cpu: CpuEntity?, gpu: GpuEntity?): Int {
        val cpuTdp = cpu?.tdp ?: 0
        val gpuTdp = gpu?.tdp ?: 0
        val basePower = 100 // mobo + RAM + storage + fans overhead
        return cpuTdp + gpuTdp + basePower
    }

    // ── Fans ────────────────────────────────────────────────────
    fun getAllFans(): Flow<List<FanEntity>> = db.fanDao().getAll()

    // ── Unfiltered access (for Parts List browser) ──────────────
    fun getAllMotherboards(): Flow<List<MotherboardEntity>> = db.motherboardDao().getAll()
    fun getAllRams(): Flow<List<RamEntity>> = db.ramDao().getAll()
    fun getAllCoolers(): Flow<List<CoolerEntity>> = db.coolerDao().getAll()
    fun getAllCases(): Flow<List<CaseEntity>> = db.caseDao().getAll()
    fun getAllPsus(): Flow<List<PsuEntity>> = db.psuDao().getAll()

    // ── Games ───────────────────────────────────────────────────
    fun getAllGames(): Flow<List<GameEntity>> = db.gameDao().getAll()

    // ── Bottleneck Calculator ───────────────────────────────────
    fun calculateBottleneck(cpuTier: Int, gpuTier: Int): BottleneckResult {
        val diff = cpuTier - gpuTier  // positive = CPU stronger
        val synergyPercentage = (100 - (kotlin.math.abs(diff) * 10)).coerceIn(0, 100)

        return when {
            diff >= 2 -> BottleneckResult(
                synergyPercentage = synergyPercentage,
                bottleneck = "GPU",
                advice = "Your CPU has plenty of headroom! " +
                         "Upgrade your GPU in the future to match your CPU's power and boost frame rates."
            )
            diff <= -2 -> BottleneckResult(
                synergyPercentage = synergyPercentage,
                bottleneck = "CPU",
                advice = "Your GPU is powerful! " +
                         "Upgrade your CPU in the future to fully unleash your GPU's potential."
            )
            else -> BottleneckResult(
                synergyPercentage = synergyPercentage,
                bottleneck = "Balanced",
                advice = "Great pairing! Your CPU and GPU are well-matched for optimal performance."
            )
        }
    }

    // ── VRM Synergy Logic ───────────────────────────────────────
    private fun calculateVrmSynergy(cpuPower: PowerTier, vrmTier: VrmTier): VrmSynergy {
        return when (cpuPower) {
            PowerTier.EXTREME -> when (vrmTier) {
                VrmTier.PREMIUM -> VrmSynergy.EXCELLENT
                VrmTier.HIGH    -> VrmSynergy.ADEQUATE
                else            -> VrmSynergy.NOT_ADVISED
            }
            PowerTier.HIGH -> when (vrmTier) {
                VrmTier.PREMIUM -> VrmSynergy.EXCELLENT
                VrmTier.HIGH    -> VrmSynergy.GOOD
                VrmTier.MID     -> VrmSynergy.ADEQUATE
                VrmTier.BASIC   -> VrmSynergy.NOT_ADVISED
            }
            PowerTier.MID -> when (vrmTier) {
                VrmTier.PREMIUM, VrmTier.HIGH -> VrmSynergy.EXCELLENT
                VrmTier.MID     -> VrmSynergy.GOOD
                VrmTier.BASIC   -> VrmSynergy.ADEQUATE
            }
            PowerTier.LOW -> VrmSynergy.EXCELLENT // Any VRM handles a low-power CPU
        }
    }
}
