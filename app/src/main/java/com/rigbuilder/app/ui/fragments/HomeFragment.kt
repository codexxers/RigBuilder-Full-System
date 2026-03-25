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
import com.rigbuilder.app.model.PreBuiltRepository
import com.rigbuilder.app.ui.adapters.FeaturedCardAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val autoScrollHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val autoScrollDelay = 5000L // 5 seconds
    private val autoScrollRunnable = Runnable { advanceToNextCard() }

    private val featuredBuilds get() = PreBuiltRepository.featuredBuilds

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
