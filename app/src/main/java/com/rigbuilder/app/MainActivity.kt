package com.rigbuilder.app

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.rigbuilder.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightStatusBars = false
        } catch (e: Exception) {
            Log.e("MainActivity", "Edge-to-edge setup failed", e)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as RigBuilderApp

        val navHostFrag = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFrag.navController

        setupDrawerNavigation()

        // Observe app readiness
        lifecycleScope.launch {
            app.isReady.collectLatest { isReady ->
                val seedError = app.seedError.value

                if (seedError != null) {
                    showError(seedError, onRetry = { app.retrySeed() })
                } else if (!isReady) {
                    showSplash()
                } else {
                    showMain()
                }
            }
        }

        // Observe seed errors
        lifecycleScope.launch {
            app.seedError.collectLatest { error ->
                if (error != null) {
                    showError(error, onRetry = { app.retrySeed() })
                } else if (app.isReady.value) {
                    showMain()
                }
            }
        }

        // Setup retry button
        binding.errorLayout.root.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.retry_button
        )?.setOnClickListener {
            app.retrySeed()
        }
    }

    private fun setupDrawerNavigation() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        // Pre-select Home if unselected
        navView.setCheckedItem(R.id.nav_home)

        navView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val popped = navController.popBackStack(R.id.homeFragment, false)
                    if (!popped) navController.navigate(R.id.homeFragment)
                }
                R.id.nav_build -> navigateSafely(R.id.buildFragment)
                R.id.nav_prebuilt -> navigateSafely(R.id.prebuiltFragment)
                R.id.nav_parts_list -> navigateSafely(R.id.partsListFragment)
                R.id.nav_laptops -> { /* Placeholder */ }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Update active drawer item based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> navView.setCheckedItem(R.id.nav_home)
                R.id.buildFragment -> navView.setCheckedItem(R.id.nav_build)
                R.id.prebuiltFragment -> navView.setCheckedItem(R.id.nav_prebuilt)
                R.id.partsListFragment -> navView.setCheckedItem(R.id.nav_parts_list)
            }
        }

        // Back button closes drawer if open
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                if (!navController.popBackStack()) {
                    finish()
                }
            }
        }
    }

    private fun navigateSafely(destinationId: Int) {
        if (navController.currentDestination?.id != destinationId) {
            // Check if it's already in the back stack to pop up to it
            val popped = navController.popBackStack(destinationId, false)
            if (!popped) {
                navController.navigate(destinationId)
            }
        }
    }

    private fun showSplash() {
        binding.splashLayout.root.visibility = View.VISIBLE
        binding.errorLayout.root.visibility = View.GONE
        binding.navHostFragment.visibility = View.GONE
    }

    private fun showError(error: String, onRetry: () -> Unit) {
        binding.splashLayout.root.visibility = View.GONE
        binding.errorLayout.root.visibility = View.VISIBLE
        binding.navHostFragment.visibility = View.GONE

        binding.errorLayout.root.findViewById<TextView>(R.id.error_message)?.text = error
        binding.errorLayout.root.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.retry_button
        )?.setOnClickListener { onRetry() }
    }

    private fun showMain() {
        binding.splashLayout.root.visibility = View.GONE
        binding.errorLayout.root.visibility = View.GONE
        binding.navHostFragment.visibility = View.VISIBLE
    }
}
