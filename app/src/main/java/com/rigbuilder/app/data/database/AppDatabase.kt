package com.rigbuilder.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rigbuilder.app.data.converter.Converters
import com.rigbuilder.app.data.dao.*
import com.rigbuilder.app.data.entity.*

@Database(
    entities = [
        CpuEntity::class,
        MotherboardEntity::class,
        RamEntity::class,
        GpuEntity::class,
        StorageEntity::class,
        CoolerEntity::class,
        CaseEntity::class,
        PsuEntity::class,
        FanEntity::class,
        GameEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cpuDao(): CpuDao
    abstract fun motherboardDao(): MotherboardDao
    abstract fun ramDao(): RamDao
    abstract fun gpuDao(): GpuDao
    abstract fun storageDao(): StorageDao
    abstract fun coolerDao(): CoolerDao
    abstract fun caseDao(): CaseDao
    abstract fun psuDao(): PsuDao
    abstract fun fanDao(): FanDao
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Suppress("DEPRECATION")
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rigbuilder_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
