package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rigbuilder.app.R
import com.rigbuilder.app.RigBuilderApp
import com.rigbuilder.app.databinding.FragmentBuildBinding
import com.rigbuilder.app.model.ComponentCategory
import com.rigbuilder.app.ui.adapters.BuildStepAdapter
import com.rigbuilder.app.ui.adapters.BuildStepItem
import com.rigbuilder.app.ui.adapters.getSelectedName
import com.rigbuilder.app.ui.adapters.getSelectedPrice
import com.rigbuilder.app.viewmodel.BuildState
import com.rigbuilder.app.viewmodel.BuildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class BuildFragment : Fragment() {

    private var _binding: FragmentBuildBinding? = null
    private val binding get() = _binding!!
    private lateinit var buildViewModel: BuildViewModel
    private lateinit var buildAdapter: BuildListAdapter
    private val pesoFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildBinding.inflate(inflater, container, false)
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
        setupRecyclerView()
        setupBottomBar()
        observeBuildState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_burger)
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as? androidx.appcompat.app.AppCompatActivity)
                ?.let { activity ->
                    val drawer = activity.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
                    drawer?.openDrawer(androidx.core.view.GravityCompat.START)
                }
        }
    }

    private fun setupRecyclerView() {
        buildAdapter = BuildListAdapter(
            onCategoryClick = { category ->
                val bundle = bundleOf("category" to category.name)
                findNavController().navigate(R.id.action_build_to_catalog, bundle)
            },
            onRemove = { category ->
                buildViewModel.removePart(category)
            }
        )
        binding.buildRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.buildRecycler.adapter = buildAdapter
    }

    private fun setupBottomBar() {
        binding.btnCanIRunIt.setOnClickListener {
            findNavController().navigate(R.id.action_build_to_performance)
        }
    }

    private fun observeBuildState() {
        viewLifecycleOwner.lifecycleScope.launch {
            buildViewModel.buildState.collectLatest { state ->
                updateBuildList(state)
                updateBottomBar(state)
                updateToolbarActions(state)
            }
        }
    }

    private fun updateBuildList(state: BuildState) {
        val items = mutableListOf<BuildListItem>()

        // Header card
        items.add(BuildListItem.Header)

        // Build steps
        ComponentCategory.entries.forEachIndexed { index, category ->
            val hasSelection = getSelectedName(category, state) != null
            val isGpuOptionalStep = category == ComponentCategory.GPU && state.gpuIsOptional && !hasSelection
            val isActive = index == state.currentStep
            val isCompleted = hasSelection
            val isLocked = !hasSelection && !isGpuOptionalStep && index > state.currentStep

            items.add(
                BuildListItem.Step(
                    BuildStepItem(
                        category = category,
                        stepNumber = index + 1,
                        isActive = isActive || isGpuOptionalStep,
                        isCompleted = isCompleted,
                        isLocked = isLocked,
                        isOptional = isGpuOptionalStep,
                        selectedName = if (isGpuOptionalStep)
                            "Optional — CPU has ${state.selectedCpu?.integratedGraphics}"
                        else getSelectedName(category, state),
                        selectedPrice = getSelectedPrice(category, state)
                    )
                )
            )
        }

        buildAdapter.submitList(items)
    }

    private fun updateBottomBar(state: BuildState) {
        if (state.totalPrice > 0) {
            binding.bottomBar.visibility = View.VISIBLE
            binding.totalPrice.text = pesoFormat.format(state.totalPrice)
            binding.totalTdp.text = "${state.totalTdp}W"
            // Show "Can I Run It?" only when build is complete
            binding.btnCanIRunIt.visibility = if (state.isComplete) View.VISIBLE else View.GONE
        } else {
            binding.bottomBar.visibility = View.GONE
        }
    }

    private fun updateToolbarActions(state: BuildState) {
        binding.toolbar.menu.clear()
        if (state.selectedCpu != null) {
            binding.toolbar.inflateMenu(R.menu.menu_build)
            binding.toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reset -> {
                        buildViewModel.resetBuild()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Multi-type adapter for header + steps + total ───────────

    sealed class BuildListItem {
        object Header : BuildListItem()
        data class Step(val item: BuildStepItem) : BuildListItem()
        data class Total(val price: Double) : BuildListItem()
    }

    inner class BuildListAdapter(
        private val onCategoryClick: (ComponentCategory) -> Unit,
        private val onRemove: (ComponentCategory) -> Unit = {}
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items: List<BuildListItem> = emptyList()
        private val stepAdapter = BuildStepAdapter(onCategoryClick, onRemove)
        private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

        fun submitList(newItems: List<BuildListItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int) = when (items[position]) {
            is BuildListItem.Header -> 0
            is BuildListItem.Step -> 1
            is BuildListItem.Total -> 2
        }

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> HeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_build_header, parent, false)
                )
                1 -> StepViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_build_step, parent, false)
                )
                2 -> TotalViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_build_total, parent, false)
                )
                else -> throw IllegalArgumentException()
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is BuildListItem.Header -> { /* Static content, no binding needed */ }
                is BuildListItem.Step -> {
                    // Delegate to the step adapter's binding logic
                    val stepHolder = BuildStepAdapter.ViewHolder(holder.itemView)
                    stepAdapter.bindStep(stepHolder, item.item)
                }
                is BuildListItem.Total -> {
                    holder.itemView.findViewById<TextView>(R.id.running_total)?.text =
                        pesoFmt.format(item.price)
                }
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class StepViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class TotalViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
