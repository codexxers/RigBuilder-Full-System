package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.rigbuilder.app.R
import com.rigbuilder.app.databinding.FragmentHomeBinding
import com.rigbuilder.app.model.PreBuiltData
import com.rigbuilder.app.ui.adapters.FeaturedCardAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val autoScrollHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val autoScrollDelay = 5000L // 5 seconds
    private val autoScrollRunnable = Runnable { advanceToNextCard() }

    private val featuredBuilds = listOf(
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupFeaturedCarousel()
        setupNavButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.title = ""
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_burger)
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as? androidx.appcompat.app.AppCompatActivity)
                ?.let { activity ->
                    val drawer = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                    drawer?.openDrawer(androidx.core.view.GravityCompat.START)
                }
        }
    }

    private fun setupFeaturedCarousel() {
        val adapter = FeaturedCardAdapter(featuredBuilds) { preBuilt ->
            val bundle = Bundle().apply {
                putString("prebuilt_id", preBuilt.id)
            }
            findNavController().navigate(R.id.action_home_to_prebuilt_detail, bundle)
        }
        binding.featuredPager.adapter = adapter
        binding.featuredPager.offscreenPageLimit = 1
        // Start at a middle position so left-swipe loops too
        val startPos = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % featuredBuilds.size)
        binding.featuredPager.setCurrentItem(startPos, false)
        // Show peek of next card
        binding.featuredPager.setPageTransformer { page, position ->
            val offset = position * -40f
            page.translationX = offset
        }

        setupDotIndicators(featuredBuilds.size)

        binding.featuredPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDotIndicators(position % featuredBuilds.size, featuredBuilds.size)
                resetAutoScroll()
            }
        })

        // Next button
        binding.btnNextCard.setOnClickListener {
            advanceToNextCard()
        }

        // Start auto-scroll
        startAutoScroll()
    }

    private fun advanceToNextCard() {
        binding.featuredPager.setCurrentItem(binding.featuredPager.currentItem + 1, true)
        resetAutoScroll()
    }

    private fun startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollDelay)
    }

    private fun resetAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollDelay)
    }

    private fun setupDotIndicators(count: Int) {
        binding.dotContainer.removeAllViews()
        val dp6 = (6 * resources.displayMetrics.density).toInt()
        val dp4 = (4 * resources.displayMetrics.density).toInt()

        repeat(count) { i ->
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp6, dp6).apply {
                    marginEnd = dp4
                    marginStart = dp4
                }
                setBackgroundResource(
                    if (i == 0) R.drawable.dot_indicator_active
                    else R.drawable.dot_indicator_inactive
                )
            }
            binding.dotContainer.addView(dot)
        }
    }

    private fun updateDotIndicators(activePos: Int, count: Int) {
        repeat(count) { i ->
            val dot = binding.dotContainer.getChildAt(i)
            dot?.setBackgroundResource(
                if (i == activePos) R.drawable.dot_indicator_active
                else R.drawable.dot_indicator_inactive
            )
        }
    }

    private fun setupNavButtons() {
        binding.btnBuildPc.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_build)
        }
        binding.btnPrebuilt.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_prebuilt)
        }
        binding.btnPartsList.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_parts_list)
        }
        binding.btnLaptops.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        startAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onDestroyView() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        super.onDestroyView()
        _binding = null
    }
}
