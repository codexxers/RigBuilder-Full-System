package com.rigbuilder.app.data.database

import android.content.Context
import android.util.Log
import com.rigbuilder.app.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class DatabaseSeeder(
    private val context: Context,
    private val database: AppDatabase
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val TAG = "DatabaseSeeder"
    }

    /**
     * Seeds every table inside a single Room transaction.
     * If any table is empty (e.g. after destructive migration), wipe and re-seed all.
     */
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val cpuCount = database.cpuDao().getCount()
            val gpuCount = database.gpuDao().getCount()
            val gameCount = database.gameDao().getCount()
            val moboCount = database.motherboardDao().getCount()

            Log.d(TAG, "Table counts — CPUs:$cpuCount GPUs:$gpuCount Games:$gameCount Mobos:$moboCount")

            if (cpuCount > 0 && gpuCount > 0 && gameCount > 0 && moboCount > 0) {
                Log.d(TAG, "Database already seeded — skipping")
                return@withContext
            }

            // If partially seeded (some tables have data, others don't), wipe and start fresh
            Log.d(TAG, "Database needs seeding — clearing all tables first…")
            database.clearAllTables()

            Log.d(TAG, "Seeding database…")
            database.runInTransaction {
                seedSync()
            }
            Log.d(TAG, "Seeding complete ✓")
        } catch (e: Exception) {
            Log.e(TAG, "Seeding FAILED — will retry on next launch", e)
            // Wipe partially-written data so getCount() == 0 on next launch
            try { database.clearAllTables() } catch (_: Exception) {}
            // Re-throw so the caller (RigBuilderApp) can display the error
            throw e
        }
    }

    /** Runs all inserts synchronously (called inside a Room transaction). */
    private fun seedSync() {
        seedTable("cpus.json") { data ->
            val items = json.decodeFromString<List<CpuEntity>>(data)
            database.cpuDao().insertAll(items)
            Log.d(TAG, "  ✓ CPUs: ${items.size} rows")
        }
        seedTable("motherboards.json") { data ->
            val items = json.decodeFromString<List<MotherboardEntity>>(data)
            database.motherboardDao().insertAll(items)
            Log.d(TAG, "  ✓ Motherboards: ${items.size} rows")
        }
        seedTable("rams.json") { data ->
            val items = json.decodeFromString<List<RamEntity>>(data)
            database.ramDao().insertAll(items)
            Log.d(TAG, "  ✓ RAMs: ${items.size} rows")
        }
        seedTable("gpus.json") { data ->
            val items = json.decodeFromString<List<GpuEntity>>(data)
            database.gpuDao().insertAll(items)
            Log.d(TAG, "  ✓ GPUs: ${items.size} rows")
        }
        seedTable("storages.json") { data ->
            val items = json.decodeFromString<List<StorageEntity>>(data)
            database.storageDao().insertAll(items)
            Log.d(TAG, "  ✓ Storages: ${items.size} rows")
        }
        seedTable("coolers.json") { data ->
            val items = json.decodeFromString<List<CoolerEntity>>(data)
            database.coolerDao().insertAll(items)
            Log.d(TAG, "  ✓ Coolers: ${items.size} rows")
        }
        seedTable("cases.json") { data ->
            val items = json.decodeFromString<List<CaseEntity>>(data)
            database.caseDao().insertAll(items)
            Log.d(TAG, "  ✓ Cases: ${items.size} rows")
        }
        seedTable("psus.json") { data ->
            val items = json.decodeFromString<List<PsuEntity>>(data)
            database.psuDao().insertAll(items)
            Log.d(TAG, "  ✓ PSUs: ${items.size} rows")
        }
        seedTable("fans.json") { data ->
            val items = json.decodeFromString<List<FanEntity>>(data)
            database.fanDao().insertAll(items)
            Log.d(TAG, "  ✓ Fans: ${items.size} rows")
        }
        seedTable("games.json") { data ->
            val items = json.decodeFromString<List<GameEntity>>(data)
            database.gameDao().insertAll(items)
            Log.d(TAG, "  ✓ Games: ${items.size} rows")
        }
    }

    /** Reads an asset file and passes its content to [block]. Throws on failure so the transaction rolls back. */
    private fun seedTable(fileName: String, block: (String) -> Unit) {
        try {
            val data = readAsset(fileName)
            block(data)
        } catch (e: Exception) {
            Log.e(TAG, "  ✗ FAILED to seed $fileName", e)
            throw e  // Re-throw so the enclosing transaction rolls back
        }
    }

    private fun readAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
