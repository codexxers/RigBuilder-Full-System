package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.rigbuilder.app.R
import com.rigbuilder.app.RigBuilderApp
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.data.repository.ComponentRepository
import com.rigbuilder.app.databinding.FragmentPartsListBinding
import com.rigbuilder.app.model.ComponentCategory
import com.rigbuilder.app.ui.adapters.ComponentAdapter
import com.rigbuilder.app.ui.adapters.ComponentItem
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PartsListFragment : Fragment() {

    private var _binding: FragmentPartsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ComponentRepository
    private lateinit var adapter: ComponentAdapter

    private var allItems: List<Any> = emptyList()
    private var selectedCategory: ComponentCategory? = null
    private var searchQuery = ""
    private var sortAscending: Boolean? = null // null = no sort, true = asc, false = desc

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as RigBuilderApp
        repository = app.repository

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
        // Sort button in toolbar
        binding.toolbar.inflateMenu(R.menu.menu_parts_list)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sort -> {
                    showSortMenu()
                    true
                }
                else -> false
            }
        }
    }

    private fun showSortMenu() {
        val anchor = binding.toolbar.findViewById<View>(R.id.action_sort) ?: binding.toolbar
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "Price: Low to High")
        popup.menu.add(0, 2, 1, "Price: High to Low")
        popup.menu.add(0, 3, 2, "Default")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { sortAscending = true; applyFilters(); true }
                2 -> { sortAscending = false; applyFilters(); true }
                3 -> { sortAscending = null; applyFilters(); true }
                else -> false
            }
        }
        popup.show()
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
        binding.categoryChipGroup.removeAllViews()

        // "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setChipBackgroundColorResource(R.color.rig_surface)
            setTextColor(resources.getColor(R.color.rig_white, null))
            setOnClickListener {
                selectedCategory = null
                updateChipSelection(this)
                applyFilters()
            }
        }
        binding.categoryChipGroup.addView(allChip)

        ComponentCategory.entries.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat.displayName
                isCheckable = true
                setChipBackgroundColorResource(R.color.rig_surface)
                setTextColor(resources.getColor(R.color.rig_white, null))
                setOnClickListener {
                    selectedCategory = cat
                    updateChipSelection(this)
                    applyFilters()
                }
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun updateChipSelection(selected: Chip) {
        for (i in 0 until binding.categoryChipGroup.childCount) {
            val chip = binding.categoryChipGroup.getChildAt(i) as? Chip
            chip?.isChecked = (chip == selected)
        }
    }

    private fun setupRecyclerView() {
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
            combine(
                repository.getAllCpus(),
                repository.getAllMotherboards(),
                repository.getAllRams(),
                repository.getAllGpus(),
                repository.getAllStorages(),
                repository.getAllCoolers(),
                repository.getAllCases(),
                repository.getAllPsus(),
                repository.getAllFans()
            ) { arrays ->
                val merged = mutableListOf<Any>()
                arrays.forEach { list ->
                    @Suppress("UNCHECKED_CAST")
                    merged.addAll(list as List<Any>)
                }
                merged
            }.collect { merged ->
                allItems = merged
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        var filtered = allItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                getName(item).contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                getCategory(item) == selectedCategory
            matchesSearch && matchesCategory
        }

        // Apply price sorting
        if (sortAscending != null) {
            filtered = if (sortAscending == true) {
                filtered.sortedBy { getPrice(it) }
            } else {
                filtered.sortedByDescending { getPrice(it) }
            }
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
        is MotherboardEntity -> item.name
        is RamEntity -> item.name
        is GpuEntity -> item.name
        is StorageEntity -> item.name
        is CoolerEntity -> item.name
        is CaseEntity -> item.name
        is PsuEntity -> item.name
        is FanEntity -> item.name
        else -> ""
    }

    private fun getCategory(item: Any): ComponentCategory? = when (item) {
        is CpuEntity -> ComponentCategory.CPU
        is MotherboardEntity -> ComponentCategory.MOTHERBOARD
        is RamEntity -> ComponentCategory.RAM
        is GpuEntity -> ComponentCategory.GPU
        is StorageEntity -> ComponentCategory.STORAGE
        is CoolerEntity -> ComponentCategory.COOLER
        is CaseEntity -> ComponentCategory.CASE
        is PsuEntity -> ComponentCategory.PSU
        is FanEntity -> ComponentCategory.FAN
        else -> null
    }

    private fun getPrice(item: Any): Double = when (item) {
        is CpuEntity -> item.price
        is MotherboardEntity -> item.price
        is RamEntity -> item.price
        is GpuEntity -> item.price
        is StorageEntity -> item.price
        is CoolerEntity -> item.price
        is CaseEntity -> item.price
        is PsuEntity -> item.price
        is FanEntity -> item.price
        else -> 0.0
    }

    private fun getItemId(item: Any, fallback: Int): Long = when (item) {
        is CpuEntity -> item.id.toLong()
        is MotherboardEntity -> item.id.toLong()
        is RamEntity -> item.id.toLong()
        is GpuEntity -> item.id.toLong()
        is StorageEntity -> item.id.toLong()
        is CoolerEntity -> item.id.toLong()
        is CaseEntity -> item.id.toLong()
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
