package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.rigbuilder.app.R
import com.rigbuilder.app.RigBuilderApp
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.data.repository.CaseCompatibility
import com.rigbuilder.app.data.repository.MotherboardWithSynergy
import com.rigbuilder.app.databinding.FragmentPartsListBinding
import com.rigbuilder.app.model.ComponentCategory
import com.rigbuilder.app.ui.adapters.ComponentAdapter
import com.rigbuilder.app.ui.adapters.ComponentItem
import com.rigbuilder.app.viewmodel.BuildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PartsListFragment : Fragment() {

    private var _binding: FragmentPartsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var buildViewModel: BuildViewModel
    private lateinit var adapter: ComponentAdapter
    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    private var allItems: List<Any> = emptyList()
    private var selectedCategory: ComponentCategory? = null
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as RigBuilderApp
        buildViewModel = ViewModelProvider(
            requireActivity(),
            BuildViewModel.Factory(app.repository)
        )[BuildViewModel::class.java]

        setupToolbar()
        setupSearch()
        setupCategoryChips()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_burger)
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as? androidx.appcompat.app.AppCompatActivity)
                ?.let { activity ->
                    val drawer = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                    drawer?.openDrawer(androidx.core.view.GravityCompat.START)
                }
        }
    }

    private fun setupSearch() {
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryChips() {
        // "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                selectedCategory = null
                applyFilters()
            }
        }
        binding.categoryChipGroup.addView(allChip)

        ComponentCategory.entries.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat.displayName
                isCheckable = true
                setOnClickListener {
                    selectedCategory = cat
                    applyFilters()
                }
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun setupRecyclerView() {
        // Read-only mode: no onAddClick action, full spec only
        adapter = ComponentAdapter(
            onAddClick = { _, _ -> /* read-only, no-op */ },
            onFullSpec = { data -> showFullSpec(data) },
            isReadOnly = true
        )
        binding.partsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.partsRecycler.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            buildViewModel.cpus.map { it as List<Any> }
                .collect { cpuList ->
                    // Collect all category data and merge
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Collect all components across categories
            val flows = listOf(
                buildViewModel.cpus.map { it as List<Any> },
                buildViewModel.motherboards.map { it as List<Any> },
                buildViewModel.rams.map { it as List<Any> },
                buildViewModel.gpus.map { it as List<Any> },
                buildViewModel.storages.map { it as List<Any> },
                buildViewModel.coolers.map { it as List<Any> },
                buildViewModel.cases.map { it as List<Any> },
                buildViewModel.psus.map { it as List<Any> },
                buildViewModel.fans.map { it as List<Any> }
            )

            // Observe each category and aggregate
            buildViewModel.cpus.collectLatest { cpus ->
                val merged = mutableListOf<Any>()
                merged.addAll(cpus)
                merged.addAll(buildViewModel.motherboards.value)
                merged.addAll(buildViewModel.rams.value)
                merged.addAll(buildViewModel.gpus.value)
                merged.addAll(buildViewModel.storages.value)
                merged.addAll(buildViewModel.coolers.value)
                merged.addAll(buildViewModel.cases.value)
                merged.addAll(buildViewModel.psus.value)
                merged.addAll(buildViewModel.fans.value)
                allItems = merged
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                getName(item).contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                getCategory(item) == selectedCategory
            matchesSearch && matchesCategory
        }

        val componentItems = filtered.mapIndexed { idx, data ->
            ComponentItem(id = getItemId(data, idx), data = data)
        }

        adapter.submitList(componentItems)
        binding.emptyState.visibility = if (componentItems.isEmpty()) View.VISIBLE else View.GONE
        binding.partsRecycler.visibility = if (componentItems.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun getName(item: Any): String = when (item) {
        is CpuEntity -> item.name
        is MotherboardWithSynergy -> item.motherboard.name
        is RamEntity -> item.name
        is GpuEntity -> item.name
        is StorageEntity -> item.name
        is CoolerEntity -> item.name
        is CaseCompatibility -> item.case.name
        is PsuEntity -> item.name
        is FanEntity -> item.name
        else -> ""
    }

    private fun getCategory(item: Any): ComponentCategory? = when (item) {
        is CpuEntity -> ComponentCategory.CPU
        is MotherboardWithSynergy -> ComponentCategory.MOTHERBOARD
        is RamEntity -> ComponentCategory.RAM
        is GpuEntity -> ComponentCategory.GPU
        is StorageEntity -> ComponentCategory.STORAGE
        is CoolerEntity -> ComponentCategory.COOLER
        is CaseCompatibility -> ComponentCategory.CASE
        is PsuEntity -> ComponentCategory.PSU
        is FanEntity -> ComponentCategory.FAN
        else -> null
    }

    private fun getItemId(item: Any, fallback: Int): Long = when (item) {
        is CpuEntity -> item.id.toLong()
        is MotherboardWithSynergy -> item.motherboard.id.toLong()
        is RamEntity -> item.id.toLong()
        is GpuEntity -> item.id.toLong()
        is StorageEntity -> item.id.toLong()
        is CoolerEntity -> item.id.toLong()
        is CaseCompatibility -> item.case.id.toLong()
        is PsuEntity -> item.id.toLong()
        is FanEntity -> item.id.toLong()
        else -> fallback.toLong()
    }

    private fun showFullSpec(data: Any) {
        val category = getCategory(data) ?: ComponentCategory.CPU
        val sheet = FullSpecBottomSheet.newInstanceReadOnly(data, category)
        sheet.show(parentFragmentManager, "full_spec_parts")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
