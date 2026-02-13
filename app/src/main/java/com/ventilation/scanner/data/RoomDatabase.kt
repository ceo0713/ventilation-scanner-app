package com.ventilation.scanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        ScanData::class,
        VentilationConfig::class,
        SimulationResult::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun scanDao(): ScanDao
    abstract fun ventilationConfigDao(): VentilationConfigDao
    abstract fun simulationResultDao(): SimulationResultDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE simulation_results ADD COLUMN deadZonePercentage REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE simulation_results ADD COLUMN virusConcentrationField TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE simulation_results ADD COLUMN ventilationScore INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(
            context: Context,
            scope: CoroutineScope? = null
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ventilation_scanner_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@androidx.room.Dao
interface ScanDao {
    @androidx.room.Query("SELECT * FROM scans ORDER BY timestamp DESC")
    suspend fun getAllScans(): List<ScanData>
    
    @androidx.room.Query("SELECT * FROM scans WHERE id = :scanId")
    suspend fun getScanById(scanId: Long): ScanData?
    
    @androidx.room.Insert
    suspend fun insertScan(scan: ScanData): Long
    
    @androidx.room.Delete
    suspend fun deleteScan(scan: ScanData)
}

@androidx.room.Dao
interface VentilationConfigDao {
    @androidx.room.Query("SELECT * FROM ventilation_configs ORDER BY timestamp DESC")
    suspend fun getAllConfigs(): List<VentilationConfig>
    
    @androidx.room.Query("SELECT * FROM ventilation_configs WHERE scanId = :scanId ORDER BY timestamp DESC")
    suspend fun getConfigsForScan(scanId: Long): List<VentilationConfig>
    
    @androidx.room.Query("SELECT * FROM ventilation_configs WHERE id = :configId")
    suspend fun getConfigById(configId: Long): VentilationConfig?
    
    @androidx.room.Insert
    suspend fun insertConfig(config: VentilationConfig): Long
    
    @androidx.room.Delete
    suspend fun deleteConfig(config: VentilationConfig)
}

@androidx.room.Dao
interface SimulationResultDao {
    @androidx.room.Query("SELECT * FROM simulation_results ORDER BY timestamp DESC")
    suspend fun getAllResults(): List<SimulationResult>
    
    @androidx.room.Query("SELECT * FROM simulation_results WHERE configId = :configId ORDER BY timestamp DESC")
    suspend fun getResultsForConfig(configId: Long): List<SimulationResult>
    
    @androidx.room.Query("SELECT * FROM simulation_results WHERE id = :resultId")
    suspend fun getResultById(resultId: Long): SimulationResult?
    
    @androidx.room.Insert
    suspend fun insertResult(result: SimulationResult): Long
    
    @androidx.room.Delete
    suspend fun deleteResult(result: SimulationResult)
}
