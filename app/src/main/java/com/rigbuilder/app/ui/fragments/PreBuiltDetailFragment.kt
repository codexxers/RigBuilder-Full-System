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
import com.rigbuilder.app.R
import com.rigbuilder.app.databinding.FragmentPrebuiltDetailBinding
import com.rigbuilder.app.model.PreBuiltData
import java.text.NumberFormat
import java.util.Locale

class PreBuiltDetailFragment : Fragment() {

    private var _binding: FragmentPrebuiltDetailBinding? = null
    private val binding get() = _binding!!
    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    private val allPreBuilts = listOf(
        PreBuiltData("pb_001", "Entry Performer", "Budget", 28000, "AMD Ryzen 5 5500", "NVIDIA GTX 1650", "16GB DDR4 3200MHz", "500GB NVMe SSD", "Seasonic B12-550", "Deepcool CC560", 5, 3, 2, 2),
        PreBuiltData("pb_002", "Solid Performance Build", "Mid-Range", 55000, "AMD Ryzen 5 5600", "NVIDIA RTX 3060", "16GB DDR4 3600MHz", "1TB NVMe SSD", "Corsair CV650", "NZXT H510", 6, 6, 3, 3),
        PreBuiltData("pb_003", "Power House", "Mid-Range", 80000, "Intel Core i7-12700K", "NVIDIA RTX 3070 Ti", "32GB DDR5 5200MHz", "2TB NVMe SSD", "Seasonic Focus GX-750", "Fractal Meshify C", 8, 8, 4, 4),
        PreBuiltData("pb_004", "Ultimate Rig", "High-End", 145000, "AMD Ryzen 9 7900X", "NVIDIA RTX 4080", "32GB DDR5 6000MHz", "2TB Gen4 NVMe SSD", "Corsair HX1000 80+ Platinum", "Lian Li O11 Dynamic", 10, 10, 5, 5)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrebuiltDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prebuiltId = arguments?.getString("prebuilt_id")
        val preBuilt = allPreBuilts.find { it.id == prebuiltId } ?: allPreBuilts[0]

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
        buildSpecRows(preBuilt)
    }

    private fun buildSpecRows(preBuilt: PreBuiltData) {
        binding.specRowsContainer.removeAllViews()
        val specs = listOf(
            "CPU" to preBuilt.cpu,
            "GPU" to preBuilt.gpu,
            "RAM" to preBuilt.ram,
            "Storage" to preBuilt.storage,
            "PSU" to preBuilt.psu,
            "Case" to preBuilt.case_
        )
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
            Toast.makeText(requireContext(), "Configure Build — Coming soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnCheckPerformance.setOnClickListener {
            Toast.makeText(requireContext(), "Performance Check — Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
