package com.rigbuilder.app

import android.app.Application
import android.util.Log
import com.rigbuilder.app.data.database.AppDatabase
import com.rigbuilder.app.data.database.DatabaseSeeder
import com.rigbuilder.app.data.repository.ComponentRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RigBuilderApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ComponentRepository(database) }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Uncaught coroutine exception", throwable)
        _seedError.value = "Coroutine error: ${throwable.message}"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _seedError = MutableStateFlow<String?>(null)
    val seedError: StateFlow<String?> = _seedError.asStateFlow()

    override fun onCreate() {
        super.onCreate()

        // Catch ALL uncaught exceptions (including Compose recomposition crashes)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "UNCAUGHT EXCEPTION on thread ${thread.name}", throwable)
            // Let the default handler deal with it (will show crash dialog or restart)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        performSeed()
    }

    fun retrySeed() {
        _seedError.value = null
        _isReady.value = false
        performSeed()
    }

    private fun performSeed() {
        applicationScope.launch {
            try {
                Log.d(TAG, "Starting database seed...")
                DatabaseSeeder(this@RigBuilderApp, database).seedIfNeeded()
                Log.d(TAG, "Seed completed successfully")
                _seedError.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Fatal seeding error", e)
                _seedError.value = "Database setup failed:\n${e.message}\n\nTry clearing app data or reinstalling."
            } finally {
                _isReady.value = true
                Log.d(TAG, "App ready = true, seedError = ${_seedError.value}")
            }
        }
    }

    companion object {
        private const val TAG = "RigBuilderApp"
    }
}
