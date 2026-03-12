package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.RangeSlider
import com.rigbuilder.app.R
import com.rigbuilder.app.RigBuilderApp
import com.rigbuilder.app.databinding.FragmentCatalogBinding
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.data.repository.CaseCompatibility
import com.rigbuilder.app.data.repository.MotherboardWithSynergy
import com.rigbuilder.app.model.ComponentCategory
import com.rigbuilder.app.ui.adapters.ComponentAdapter
import com.rigbuilder.app.ui.adapters.ComponentItem
import com.rigbuilder.app.viewmodel.BuildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var buildViewModel: BuildViewModel
    private lateinit var adapter: ComponentAdapter
    private lateinit var category: ComponentCategory
    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    // Filter state
    private var searchQuery = ""
    private var selectedBrands = mutableSetOf<String>()
    private var priceMin = 0f
    private var priceMax = 200000f
    private var sortAscending = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getString("category") ?: return
        category = try {
            ComponentCategory.valueOf(categoryName)
        } catch (e: Exception) {
            return
        }

        val app = requireActivity().application as RigBuilderApp
        buildViewModel = ViewModelProvider(
            requireActivity(),
            BuildViewModel.Factory(app.repository)
        )[BuildViewModel::class.java]

        setupToolbar()
        setupSearch()
        setupFilterPanel()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbar.title = category.displayName
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbar.inflateMenu(R.menu.menu_catalog)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter -> {
                    toggleFilterPanel()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearch() {
        binding.searchEdit.hint = "Search ${category.displayName}..."
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilterPanel() {
        val filterView = binding.filterPanel.root

        // Sort buttons
        val btnAsc = filterView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_sort_asc)
        val btnDesc = filterView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_sort_desc)
        val btnReset = filterView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_filter_reset)

        updateSortButtons(btnAsc, btnDesc)

        btnAsc.setOnClickListener {
            if (!sortAscending) {
                sortAscending = true
                updateSortButtons(btnAsc, btnDesc)
                applyFilters()
            }
        }
        btnDesc.setOnClickListener {
            if (sortAscending) {
                sortAscending = false
                updateSortButtons(btnAsc, btnDesc)
                applyFilters()
            }
        }

        // Price slider
        val priceSlider = filterView.findViewById<RangeSlider>(R.id.price_slider)
        val priceMinLabel = filterView.findViewById<TextView>(R.id.price_min_label)
        val priceMaxLabel = filterView.findViewById<TextView>(R.id.price_max_label)

        priceSlider.values = listOf(0f, 200000f)
        priceSlider.stepSize = 5000f
        priceMinLabel.text = pesoFmt.format(0.0)
        priceMaxLabel.text = pesoFmt.format(200000.0)

        priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            priceMin = values[0]
            priceMax = values[1]
            priceMinLabel.text = pesoFmt.format(priceMin.toDouble())
            priceMaxLabel.text = pesoFmt.format(priceMax.toDouble())
            applyFilters()
        }

        // Reset
        btnReset.setOnClickListener {
            selectedBrands.clear()
            priceMin = 0f
            priceMax = 200000f
            sortAscending = true
            priceSlider.values = listOf(0f, 200000f)
            updateSortButtons(btnAsc, btnDesc)
            rebuildBrandCheckboxes()
            applyFilters()
        }
    }

    private fun updateSortButtons(
        btnAsc: com.google.android.material.button.MaterialButton,
        btnDesc: com.google.android.material.button.MaterialButton
    ) {
        val ctx = requireContext()
        if (sortAscending) {
            btnAsc.setTextColor(ContextCompat.getColor(ctx, R.color.rig_red))
            btnAsc.strokeColor = ContextCompat.getColorStateList(ctx, R.color.rig_red)
            btnDesc.setTextColor(ContextCompat.getColor(ctx, R.color.rig_gray))
            btnDesc.strokeColor = ContextCompat.getColorStateList(ctx, R.color.rig_gray_dark)
        } else {
            btnDesc.setTextColor(ContextCompat.getColor(ctx, R.color.rig_red))
            btnDesc.strokeColor = ContextCompat.getColorStateList(ctx, R.color.rig_red)
            btnAsc.setTextColor(ContextCompat.getColor(ctx, R.color.rig_gray))
            btnAsc.strokeColor = ContextCompat.getColorStateList(ctx, R.color.rig_gray_dark)
        }
    }

    private fun toggleFilterPanel() {
        binding.filterPanel.root.visibility =
            if (binding.filterPanel.root.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun setupRecyclerView() {
        adapter = ComponentAdapter(
            onAddClick = { data, variant -> handleAdd(data, variant) },
            onFullSpec = { data -> showFullSpec(data) }
        )
        binding.componentRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.componentRecycler.adapter = adapter
    }

    // All raw items from the ViewModel (unfiltered)
    private var allItems: List<Any> = emptyList()

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Map each type-specific StateFlow to StateFlow<List<Any>> to avoid type mismatch
            val anyFlow = when (category) {
                ComponentCategory.CPU -> buildViewModel.cpus.map { it as List<Any> }
                ComponentCategory.MOTHERBOARD -> buildViewModel.motherboards.map { it as List<Any> }
                ComponentCategory.RAM -> buildViewModel.rams.map { it as List<Any> }
                ComponentCategory.GPU -> buildViewModel.gpus.map { it as List<Any> }
                ComponentCategory.STORAGE -> buildViewModel.storages.map { it as List<Any> }
                ComponentCategory.COOLER -> buildViewModel.coolers.map { it as List<Any> }
                ComponentCategory.CASE -> buildViewModel.cases.map { it as List<Any> }
                ComponentCategory.PSU -> buildViewModel.psus.map { it as List<Any> }
                ComponentCategory.FAN -> buildViewModel.fans.map { it as List<Any> }
            }

            // Combine items with build state for PSU recommended watts / fan slots
            combine(anyFlow, buildViewModel.buildState) { items, state ->
                Pair(items, state)
            }.collectLatest { (items, state) ->
                allItems = items
                currentRecommendedPsu = state.recommendedPsuWatts
                currentFanSlots = state.availableFanSlots
                updateBrands()
                applyFilters()
            }
        }
    }

    private var currentRecommendedPsu = 0
    private var currentFanSlots = 0

    private fun updateBrands() {
        val brands = allItems.mapNotNull { item ->
            when (item) {
                is CpuEntity -> item.brand
                is MotherboardWithSynergy -> item.motherboard.brand
                is RamEntity -> item.brand
                is GpuEntity -> item.brand
                is StorageEntity -> item.brand
                is CoolerEntity -> item.brand
                is CaseCompatibility -> item.case.brand
                is PsuEntity -> item.brand
                is FanEntity -> item.brand
                else -> null
            }
        }.distinct()

        val filterView = binding.filterPanel.root
        val brandLabel = filterView.findViewById<TextView>(R.id.brand_label)
        val brandContainer = filterView.findViewById<LinearLayout>(R.id.brand_container)

        if (brands.isNotEmpty()) {
            brandLabel.visibility = View.VISIBLE
            brandContainer.visibility = View.VISIBLE
            rebuildBrandCheckboxes(brands, brandContainer)
        } else {
            brandLabel.visibility = View.GONE
            brandContainer.visibility = View.GONE
        }
    }

    private var currentBrands: List<String> = emptyList()

    private fun rebuildBrandCheckboxes(
        brands: List<String> = currentBrands,
        container: LinearLayout? = null
    ) {
        currentBrands = brands
        val brandContainer = container ?: binding.filterPanel.root.findViewById(R.id.brand_container) ?: return
        brandContainer.removeAllViews()

        brands.chunked(2).forEach { row ->
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.forEach { brand ->
                val cb = CheckBox(requireContext()).apply {
                    text = brand
                    isChecked = brand in selectedBrands
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_white))
                    buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.rig_red)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedBrands.add(brand) else selectedBrands.remove(brand)
                        applyFilters()
                    }
                }
                rowLayout.addView(cb)
            }
            if (row.size == 1) {
                rowLayout.addView(View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
                })
            }
            brandContainer.addView(rowLayout)
        }
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val (brand, price, name) = extractBrandPriceName(item)
            val matchesSearch = searchQuery.isBlank() || name.contains(searchQuery, true) || brand.contains(searchQuery, true)
            val matchesBrand = selectedBrands.isEmpty() || brand in selectedBrands
            val matchesPrice = price in priceMin.toDouble()..priceMax.toDouble()
            matchesSearch && matchesBrand && matchesPrice
        }.let { list ->
            if (sortAscending) list.sortedBy { extractBrandPriceName(it).second }
            else list.sortedByDescending { extractBrandPriceName(it).second }
        }

        val componentItems = filtered.mapIndexed { index, data ->
            ComponentItem(
                id = getItemId(data, index),
                data = data,
                recommendedPsuWatts = currentRecommendedPsu,
                availableFanSlots = currentFanSlots
            )
        }

        adapter.submitList(componentItems)
        binding.emptyState.visibility = if (componentItems.isEmpty()) View.VISIBLE else View.GONE
        binding.componentRecycler.visibility = if (componentItems.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun extractBrandPriceName(item: Any): Triple<String, Double, String> = when (item) {
        is CpuEntity -> Triple(item.brand, item.price, item.name)
        is MotherboardWithSynergy -> Triple(item.motherboard.brand, item.motherboard.price, item.motherboard.name)
        is RamEntity -> Triple(item.brand, item.price, item.name)
        is GpuEntity -> Triple(item.brand, item.price, item.name)
        is StorageEntity -> Triple(item.brand, item.price, item.name)
        is CoolerEntity -> Triple(item.brand, item.price, item.name)
        is CaseCompatibility -> Triple(item.case.brand, item.case.price, item.case.name)
        is PsuEntity -> Triple(item.brand, item.price, item.name)
        is FanEntity -> Triple(item.brand, item.price, item.name)
        else -> Triple("", 0.0, "")
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

    private fun handleAdd(data: Any, variant: String?) {
        when (data) {
            is CpuEntity -> buildViewModel.selectCpu(data)
            is MotherboardWithSynergy -> buildViewModel.selectMotherboard(data.motherboard, data.synergy)
            is RamEntity -> variant?.let { buildViewModel.selectRam(data, it) }
            is GpuEntity -> buildViewModel.selectGpu(data)
            is StorageEntity -> variant?.let { buildViewModel.selectStorage(data, it) }
            is CoolerEntity -> buildViewModel.selectCooler(data)
            is CaseCompatibility -> variant?.let { buildViewModel.selectCase(data.case, it) }
            is PsuEntity -> buildViewModel.selectPsu(data)
            is FanEntity -> {
                val packs = variant?.toIntOrNull() ?: 1
                buildViewModel.selectFan(data, packs)
            }
        }
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun showFullSpec(data: Any) {
        val sheet = FullSpecBottomSheet.newInstance(data, category, buildViewModel)
        sheet.onAddCallback = { variant ->
            handleAdd(data, variant)
        }
        sheet.show(parentFragmentManager, "full_spec")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
