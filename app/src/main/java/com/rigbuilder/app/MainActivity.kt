package com.rigbuilder.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.rigbuilder.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
