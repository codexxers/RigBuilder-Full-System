package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rigbuilder.app.R
import com.rigbuilder.app.databinding.FragmentPrebuiltBinding
import com.rigbuilder.app.model.PreBuiltData
import com.rigbuilder.app.ui.adapters.PreBuiltAdapter

class PreBuiltFragment : Fragment() {

    private var _binding: FragmentPrebuiltBinding? = null
    private val binding get() = _binding!!

    private val preBuiltList = listOf(
        PreBuiltData(
            id = "pb_001", name = "Entry Performer", tier = "Budget", price = 28000,
            cpu = "AMD Ryzen 5 5500", gpu = "NVIDIA GTX 1650",
            ram = "16GB DDR4 3200MHz", storage = "500GB NVMe SSD",
            psu = "Seasonic B12-550", case_ = "Deepcool CC560",
            cpuTier = 5, gpuTier = 3, vrmTier = 2, psuTier = 2
        ),
        PreBuiltData(
            id = "pb_002", name = "Solid Performance Build", tier = "Mid-Range", price = 55000,
            cpu = "AMD Ryzen 5 5600", gpu = "NVIDIA RTX 3060",
            ram = "16GB DDR4 3600MHz", storage = "1TB NVMe SSD",
            psu = "Corsair CV650", case_ = "NZXT H510",
            cpuTier = 6, gpuTier = 6, vrmTier = 3, psuTier = 3
        ),
        PreBuiltData(
            id = "pb_003", name = "Power House", tier = "Mid-Range", price = 80000,
            cpu = "Intel Core i7-12700K", gpu = "NVIDIA RTX 3070 Ti",
            ram = "32GB DDR5 5200MHz", storage = "2TB NVMe SSD",
            psu = "Seasonic Focus GX-750", case_ = "Fractal Meshify C",
            cpuTier = 8, gpuTier = 8, vrmTier = 4, psuTier = 4
        ),
        PreBuiltData(
            id = "pb_004", name = "Ultimate Rig", tier = "High-End", price = 145000,
            cpu = "AMD Ryzen 9 7900X", gpu = "NVIDIA RTX 4080",
            ram = "32GB DDR5 6000MHz", storage = "2TB Gen4 NVMe SSD",
            psu = "Corsair HX1000 80+ Platinum", case_ = "Lian Li O11 Dynamic",
            cpuTier = 10, gpuTier = 10, vrmTier = 5, psuTier = 5
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrebuiltBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
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

    private fun setupRecyclerView() {
        val adapter = PreBuiltAdapter(preBuiltList) { preBuilt ->
            val bundle = Bundle().apply {
                putString("prebuilt_id", preBuilt.id)
            }
            findNavController().navigate(R.id.action_prebuilt_to_detail, bundle)
        }
        binding.prebuiltRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.prebuiltRecycler.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
