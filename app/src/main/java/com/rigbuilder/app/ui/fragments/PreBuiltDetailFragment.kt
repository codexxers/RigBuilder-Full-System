package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rigbuilder.app.R
import com.rigbuilder.app.RigBuilderApp
import com.rigbuilder.app.databinding.FragmentPrebuiltDetailBinding
import com.rigbuilder.app.model.PreBuiltData
import com.rigbuilder.app.model.PreBuiltRepository
import com.rigbuilder.app.viewmodel.BuildViewModel
import java.text.NumberFormat
import java.util.Locale

class PreBuiltDetailFragment : Fragment() {

    private var _binding: FragmentPrebuiltDetailBinding? = null
    private val binding get() = _binding!!
    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    private lateinit var buildViewModel: BuildViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrebuiltDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as RigBuilderApp
        buildViewModel = ViewModelProvider(
            requireActivity(),
            BuildViewModel.Factory(app.repository)
        )[BuildViewModel::class.java]

        val prebuiltId = arguments?.getString("prebuilt_id")
        val preBuilt = PreBuiltRepository.findById(prebuiltId ?: "") ?: PreBuiltRepository.allPreBuilts[0]

        setupToolbar(preBuilt.name)
        bindDetail(preBuilt)
        setupButtons(preBuilt)
    }

    private fun setupToolbar(title: String) {
        binding.toolbar.title = title
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_burger)
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as? androidx.appcompat.app.AppCompatActivity)
                ?.let { activity ->
                    val drawer = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                    drawer?.openDrawer(androidx.core.view.GravityCompat.START)
                }
        }
    }

    private fun bindDetail(preBuilt: PreBuiltData) {
        binding.detailPcName.text = preBuilt.name
        binding.detailTierBadge.text = preBuilt.tier
        binding.detailPrice.text = pesoFmt.format(preBuilt.price.toDouble())
        binding.detailDescription.text = preBuilt.description
        buildSpecRows(preBuilt)
    }

    private fun buildSpecRows(preBuilt: PreBuiltData) {
        binding.specRowsContainer.removeAllViews()
        val specs = mutableListOf(
            "CPU" to preBuilt.cpu,
            "GPU" to preBuilt.gpu,
            "RAM" to preBuilt.ram,
            "Storage" to preBuilt.storage,
            "PSU" to preBuilt.psu,
            "Case" to preBuilt.case_
        )
        if (preBuilt.motherboard.isNotBlank()) specs.add(2, "Board" to preBuilt.motherboard)
        if (preBuilt.cooler.isNotBlank()) specs.add("Cooler" to preBuilt.cooler)
        if (preBuilt.fans.isNotBlank()) specs.add("Fans" to preBuilt.fans)

        specs.forEach { (label, value) ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(8), 0, dp(8))
            }
            val labelTv = TextView(requireContext()).apply {
                text = label
                setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_gray))
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(dp(90), LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val valueTv = TextView(requireContext()).apply {
                text = value
                setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_white))
                textSize = 14f
                paint.isFakeBoldText = true
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(labelTv)
            row.addView(valueTv)
            binding.specRowsContainer.addView(row)

            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
                )
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rig_surface_variant))
                alpha = 0.4f
            }
            binding.specRowsContainer.addView(divider)
        }
    }

    private fun setupButtons(preBuilt: PreBuiltData) {
        var specExpanded = false
        binding.btnFullSpec.setOnClickListener {
            specExpanded = !specExpanded
            binding.fullSpecPanel.visibility = if (specExpanded) View.VISIBLE else View.GONE
            binding.btnFullSpec.text = if (specExpanded) "Hide Spec" else "Full Spec"
        }
        binding.btnConfigure.setOnClickListener {
            // Reset the build and load this pre-built's components
            buildViewModel.resetBuild()
            buildViewModel.loadPreBuilt(preBuilt)
            // Navigate to the Build screen
            findNavController().navigate(R.id.action_prebuilt_detail_to_build)
        }
        binding.btnCheckPerformance.setOnClickListener {
            buildViewModel.resetBuild()
            buildViewModel.loadPreBuilt(preBuilt)
            findNavController().navigate(R.id.action_prebuilt_detail_to_performance)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

