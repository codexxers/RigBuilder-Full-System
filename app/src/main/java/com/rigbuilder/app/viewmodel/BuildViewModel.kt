package com.rigbuilder.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.data.repository.*
import com.rigbuilder.app.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BuildState(
    val selectedCpu: CpuEntity? = null,
    val selectedMotherboard: MotherboardEntity? = null,
    val selectedMotherboardSynergy: VrmSynergy? = null,
    val selectedRam: RamEntity? = null,
    val selectedRamVariant: String? = null,
    val selectedGpu: GpuEntity? = null,
    val selectedStorage: StorageEntity? = null,
    val selectedStorageVariant: String? = null,
    val selectedCooler: CoolerEntity? = null,
    val selectedCase: CaseEntity? = null,
    val selectedCaseVariant: String? = null,
    val selectedPsu: PsuEntity? = null,
    val selectedFan: FanEntity? = null,
    val selectedFanQuantity: Int = 0
) {
    /** True when CPU has a gaming-capable iGPU (G-suffix models like 8500G, 8600G, 8700G). */
    val hasGamingIgpu: Boolean
        get() = selectedCpu?.integratedGraphics != null &&
            selectedCpu.name.endsWith("G") &&
            (selectedCpu.integratedGraphics?.contains("Radeon", ignoreCase = true) == true)

    /** GPU step is optional when the CPU has a gaming iGPU. */
    val gpuIsOptional: Boolean
        get() = hasGamingIgpu

    /** The first unfilled step — drives the stepper UI. Skips GPU when iGPU present. */
    val currentStep: Int
        get() {
            if (selectedCpu == null) return 0
            if (selectedMotherboard == null) return 1
            if (selectedRam == null) return 2
            // Skip GPU requirement when CPU has gaming iGPU
            if (selectedGpu == null && !gpuIsOptional) return 3
            if (selectedStorage == null) return 4
            if (selectedCooler == null) return 5
            if (selectedCase == null) return 6
            if (selectedPsu == null) return 7
            if (selectedFan == null) return 8
            return 9 // all done
        }

    val totalPrice: Double
        get() = listOfNotNull(
            selectedCpu?.price,
            selectedMotherboard?.price,
            selectedRam?.price,
            selectedGpu?.price,
            selectedStorage?.price,
            selectedCooler?.price,
            selectedCase?.price,
            selectedPsu?.price,
            selectedFan?.let { it.price * selectedFanQuantity / it.quantity }
        ).sum()

    val totalTdp: Int
        get() = (selectedCpu?.tdp ?: 0) + (selectedGpu?.tdp ?: 0) + 100

    /** Uses the higher of: GPU manufacturer recommended PSU OR system TDP × 1.2 */
    val recommendedPsuWatts: Int
        get() {
            val gpuRecommended = selectedGpu?.recommendedPsuWatts ?: 0
            val systemCalculated = (totalTdp * 1.2).toInt()
            return maxOf(gpuRecommended, systemCalculated)
        }

    val isComplete: Boolean
        get() = selectedCpu != null && selectedMotherboard != null &&
                selectedRam != null && (selectedGpu != null || gpuIsOptional) &&
                selectedStorage != null && selectedCooler != null &&
                selectedCase != null && selectedPsu != null

    val availableFanSlots: Int
        get() {
            val total = selectedCase?.totalFanSlots ?: 0
            val included = selectedCase?.includedFans ?: 0
            return (total - included).coerceAtLeast(0)
        }
}

class BuildViewModel(private val repository: ComponentRepository) : ViewModel() {

    private val _buildState = MutableStateFlow(BuildState())
    val buildState: StateFlow<BuildState> = _buildState.asStateFlow()

    // ── Filtered component lists ────────────────────────────────
    val cpus: StateFlow<List<CpuEntity>> = repository.getAllCpus()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val motherboards: StateFlow<List<MotherboardWithSynergy>> = _buildState
        .map { it.selectedCpu }
        .distinctUntilChanged()
        .flatMapLatest { cpu ->
            if (cpu != null) repository.getFilteredMotherboards(cpu)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val rams: StateFlow<List<RamEntity>> = _buildState
        .map { it.selectedMotherboard }
        .distinctUntilChanged()
        .flatMapLatest { mobo ->
            if (mobo != null) repository.getFilteredRam(mobo)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val gpus: StateFlow<List<GpuEntity>> = repository.getAllGpus()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val storages: StateFlow<List<StorageEntity>> = repository.getAllStorages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val coolers: StateFlow<List<CoolerEntity>> = _buildState
        .map { it.selectedCpu?.socket }
        .distinctUntilChanged()
        .flatMapLatest { socket ->
            if (socket != null) repository.getFilteredCoolers(socket)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cases: StateFlow<List<CaseCompatibility>> = _buildState
        .map {
            Triple(
                it.selectedMotherboard?.formFactor,
                it.selectedGpu?.lengthMm ?: 0,
                if (it.selectedCooler?.type == CoolerType.AIO) 0 to (it.selectedCooler?.radiatorSizeMm ?: 0)
                else (it.selectedCooler?.heightMm ?: 0) to 0
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (ff, gpuLen, coolerDims) ->
            if (ff != null) repository.getFilteredCases(ff, gpuLen, coolerDims.first, coolerDims.second)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val psus: StateFlow<List<PsuEntity>> = _buildState
        .map { it.selectedCase?.maxPsuLengthMm ?: 999 }
        .distinctUntilChanged()
        .flatMapLatest { maxLen ->
            repository.getAllPsusFilteredByLength(maxLen)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val fans: StateFlow<List<FanEntity>> = repository.getAllFans()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ── Selection actions (smart reset — only invalidate dependents) ──
    fun selectCpu(cpu: CpuEntity) {
        _buildState.update {
            it.copy(
                selectedCpu = cpu,
                // Only Motherboard (socket/chipset) and RAM (DDR gen via mobo) depend on CPU
                selectedMotherboard = null,
                selectedMotherboardSynergy = null,
                selectedRam = null,
                selectedRamVariant = null
                // GPU, Storage, Cooler, Case, PSU, Fans are KEPT
            )
        }
    }

    fun selectMotherboard(mobo: MotherboardEntity, synergy: VrmSynergy) {
        _buildState.update {
            it.copy(
                selectedMotherboard = mobo,
                selectedMotherboardSynergy = synergy,
                // Only RAM depends on Motherboard (DDR generation)
                selectedRam = null,
                selectedRamVariant = null
                // Everything else is KEPT
            )
        }
    }

    fun selectRam(ram: RamEntity, variant: String) {
        _buildState.update {
            it.copy(selectedRam = ram, selectedRamVariant = variant)
        }
    }

    fun selectGpu(gpu: GpuEntity) {
        _buildState.update {
            it.copy(selectedGpu = gpu)
            // Case clearance is re-evaluated reactively — no need to clear case
        }
    }

    fun selectStorage(storage: StorageEntity, variant: String) {
        _buildState.update {
            it.copy(selectedStorage = storage, selectedStorageVariant = variant)
        }
    }

    fun selectCooler(cooler: CoolerEntity) {
        _buildState.update {
            it.copy(selectedCooler = cooler)
            // Case clearance is re-evaluated reactively
        }
    }

    fun selectCase(case_: CaseEntity, variant: String) {
        _buildState.update {
            it.copy(selectedCase = case_, selectedCaseVariant = variant)
            // PSU length & fan slots are re-evaluated reactively
        }
    }

    fun selectPsu(psu: PsuEntity) {
        _buildState.update {
            it.copy(selectedPsu = psu)
        }
    }

    fun selectFan(fan: FanEntity, packCount: Int) {
        _buildState.update {
            it.copy(selectedFan = fan, selectedFanQuantity = fan.quantity * packCount)
        }
    }

    fun removePart(category: ComponentCategory) {
        _buildState.update { state ->
            when (category) {
                ComponentCategory.CPU -> state.copy(selectedCpu = null)
                ComponentCategory.MOTHERBOARD -> state.copy(selectedMotherboard = null, selectedMotherboardSynergy = null)
                ComponentCategory.RAM -> state.copy(selectedRam = null, selectedRamVariant = null)
                ComponentCategory.GPU -> state.copy(selectedGpu = null)
                ComponentCategory.STORAGE -> state.copy(selectedStorage = null, selectedStorageVariant = null)
                ComponentCategory.COOLER -> state.copy(selectedCooler = null)
                ComponentCategory.CASE -> state.copy(selectedCase = null, selectedCaseVariant = null)
                ComponentCategory.PSU -> state.copy(selectedPsu = null)
                ComponentCategory.FAN -> state.copy(selectedFan = null, selectedFanQuantity = 0)
            }
        }
    }

    fun resetBuild() {
        _buildState.update { BuildState() }
    }

    /**
     * Pre-fill the build with components matching the given PreBuiltData names.
     * Queries the repository DIRECTLY (not filtered flows) so all parts are available.
     */
    fun loadPreBuilt(preBuilt: com.rigbuilder.app.model.PreBuiltData) {
        viewModelScope.launch {
            // Fetch ALL components from DB in parallel — bypass the filtered flows
            val allCpus = repository.getAllCpus().first()
            val allMobos = repository.getAllMotherboards().first()
            val allRams = repository.getAllRams().first()
            val allGpus = repository.getAllGpus().first()
            val allStorages = repository.getAllStorages().first()
            val allCoolers = repository.getAllCoolers().first()
            val allCases = repository.getAllCases().first()
            val allPsus = repository.getAllPsus().first()
            val allFans = repository.getAllFans().first()

            // Match by name
            val cpu = allCpus.firstOrNull { it.name == preBuilt.cpu }
            val mobo = allMobos.firstOrNull { it.name == preBuilt.motherboard }
            val ram = allRams.firstOrNull { it.name == preBuilt.ram }
            val gpu = allGpus.firstOrNull { it.name == preBuilt.gpu }
            val storage = allStorages.firstOrNull { it.name == preBuilt.storage }
            val cooler = allCoolers.firstOrNull { it.name == preBuilt.cooler }
            val case_ = allCases.firstOrNull { it.name == preBuilt.case_ }
            val psu = allPsus.firstOrNull { it.name == preBuilt.psu }
            val fan = allFans.firstOrNull { it.name == preBuilt.fans }

            // Set the entire build in one atomic update
            _buildState.update {
                BuildState(
                    selectedCpu = cpu,
                    selectedMotherboard = mobo,
                    selectedMotherboardSynergy = null, // synergy recalculates in UI
                    selectedRam = ram,
                    selectedRamVariant = ram?.colorVariants?.firstOrNull(),
                    selectedGpu = gpu,
                    selectedStorage = storage,
                    selectedStorageVariant = storage?.capacityVariants?.firstOrNull(),
                    selectedCooler = cooler,
                    selectedCase = case_,
                    selectedCaseVariant = case_?.colorVariants?.firstOrNull(),
                    selectedPsu = psu,
                    selectedFan = fan,
                    selectedFanQuantity = fan?.quantity ?: 0
                )
            }
        }
    }

    // ── Factory ─────────────────────────────────────────────────
    class Factory(private val repository: ComponentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BuildViewModel(repository) as T
        }
    }
}
