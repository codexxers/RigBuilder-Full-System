package com.rigbuilder.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rigbuilder.app.data.entity.GameEntity
import com.rigbuilder.app.data.repository.BottleneckResult
import com.rigbuilder.app.data.repository.ComponentRepository
import kotlinx.coroutines.flow.*

data class GamePerformance(
    val game: GameEntity,
    val canRunMin: Boolean,
    val canRunRec: Boolean,
    val cpuMeetsMin: Boolean,
    val cpuMeetsRec: Boolean,
    val gpuMeetsMin: Boolean,
    val gpuMeetsRec: Boolean,
    val ramMeetsMin: Boolean,
    val ramMeetsRec: Boolean,
    val statusLabel: String  // "Runs Great", "Playable", "Below Min Specs"
)

class PerformanceViewModel(
    private val repository: ComponentRepository,
    private val buildViewModel: BuildViewModel
) : ViewModel() {

    val games: StateFlow<List<GameEntity>> = repository.getAllGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /** Maps iGPU name to an approximate discrete GPU gaming tier based on benchmarks. */
    private fun getIgpuTier(igpuName: String?): Int {
        if (igpuName == null) return 0
        return when {
            igpuName.contains("780M", ignoreCase = true) -> 4  // ≈ GTX 1650
            igpuName.contains("760M", ignoreCase = true) -> 3  // ≈ GTX 1050
            igpuName.contains("740M", ignoreCase = true) -> 2  // ≈ GT 1030
            else -> 1 // Generic iGPU (basic display only)
        }
    }

    /** Returns the effective GPU gaming tier — discrete GPU if present, otherwise iGPU equivalent. */
    private fun getEffectiveGpuTier(state: BuildState): Int {
        if (state.selectedGpu != null) return state.selectedGpu.gamingTier
        if (state.hasGamingIgpu) return getIgpuTier(state.selectedCpu?.integratedGraphics)
        return 0
    }

    val bottleneck: StateFlow<BottleneckResult?> = buildViewModel.buildState
        .map { state ->
            val cpuTier = state.selectedCpu?.gamingTier
            val gpuTier = getEffectiveGpuTier(state).takeIf { it > 0 }
            if (cpuTier != null && gpuTier != null) {
                repository.calculateBottleneck(cpuTier, gpuTier)
            } else null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    fun evaluateGame(game: GameEntity): GamePerformance {
        val state = buildViewModel.buildState.value
        val cpuTier = state.selectedCpu?.gamingTier ?: 0
        val gpuTier = getEffectiveGpuTier(state)
        val ramGb = state.selectedRam?.capacityGb ?: 0

        val cpuMin = cpuTier >= game.minCpuTier
        val cpuRec = cpuTier >= game.recCpuTier
        val gpuMin = gpuTier >= game.minGpuTier
        val gpuRec = gpuTier >= game.recGpuTier
        val ramMin = ramGb >= game.minRamGb
        val ramRec = ramGb >= game.recRamGb

        val canMin = cpuMin && gpuMin && ramMin
        val canRec = cpuRec && gpuRec && ramRec

        val status = when {
            canRec -> "Runs Great"
            canMin -> "Playable"
            else -> "Below Min Specs"
        }

        return GamePerformance(
            game = game,
            canRunMin = canMin,
            canRunRec = canRec,
            cpuMeetsMin = cpuMin,
            cpuMeetsRec = cpuRec,
            gpuMeetsMin = gpuMin,
            gpuMeetsRec = gpuRec,
            ramMeetsMin = ramMin,
            ramMeetsRec = ramRec,
            statusLabel = status
        )
    }

    class Factory(
        private val repository: ComponentRepository,
        private val buildViewModel: BuildViewModel
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerformanceViewModel(repository, buildViewModel) as T
        }
    }
}
