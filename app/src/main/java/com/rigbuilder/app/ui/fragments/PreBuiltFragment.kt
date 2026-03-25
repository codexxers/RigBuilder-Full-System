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
import com.rigbuilder.app.model.PreBuiltRepository
import com.rigbuilder.app.ui.adapters.PreBuiltAdapter

class PreBuiltFragment : Fragment() {

    private var _binding: FragmentPrebuiltBinding? = null
    private val binding get() = _binding!!

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
        val adapter = PreBuiltAdapter(PreBuiltRepository.allPreBuilts) { preBuilt ->
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
