package com.ventilation.scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.Date

/**
 * 3D 스캔 데이터 엔티티
 */
@Entity(tableName = "scans")
@TypeConverters(Converters::class)
data class ScanData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val timestamp: Date,
    
    // 3D mesh data (vertices and faces as JSON)
    val meshVertices: String,  // JSON array of [x, y, z] points
    val meshFaces: String,      // JSON array of face indices
    
    // Bounding box
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val minZ: Float,
    val maxZ: Float,
    
    // Room dimensions
    val width: Float,   // meters
    val height: Float,  // meters
    val depth: Float    // meters
)

/**
 * 환기 설정 데이터
 */
@Entity(tableName = "ventilation_configs")
@TypeConverters(Converters::class)
data class VentilationConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val scanId: Long,  // Foreign key to ScanData
    
    // Inlets (입구 - 문, 창문 등)
    val inlets: List<VentilationOpening>,
    
    // Outlets (출구 - 환기구, 창문 등)
    val outlets: List<VentilationOpening>,
    
    // 환경 설정
    val outsideTemperature: Float = 25f,  // Celsius
    val insideTemperature: Float = 23f,
    val airVelocity: Float = 0.5f,        // m/s
    
    val timestamp: Date
)

/**
 * 환기 개구부 (문, 창문, 환기구)
 */
@kotlinx.parcelize.Parcelize
data class VentilationOpening(
    val type: OpeningType,
    val x: Float,
    val y: Float,
    val z: Float,
    val width: Float,
    val height: Float,
    val velocity: Float = 0.5f,
    val isActive: Boolean = true,
    val name: String = "",
    val openingDepth: Float = 0.1f,
    val cmh: Float = 0f,
    val temperature: Float = 0f,
    val notes: String = ""
) : Serializable, android.os.Parcelable

enum class OpeningType {
    DOOR,
    WINDOW,
    VENT,
    AC_UNIT,
    VENTILATOR,
    AIR_PURIFIER,
    AIR_STERILIZER;
    
    val isOpening: Boolean
        get() = this in listOf(DOOR, WINDOW, VENT)
    
    val isDevice: Boolean
        get() = this in listOf(AC_UNIT, VENTILATOR, AIR_PURIFIER, AIR_STERILIZER)
    
    val hasCMH: Boolean
        get() = isDevice
    
    val defaultCMH: Float
        get() = when (this) {
            AC_UNIT -> 300f
            VENTILATOR -> 200f
            AIR_PURIFIER, AIR_STERILIZER -> 150f
            else -> 0f
        }
}

/**
 * CFD 시뮬레이션 결과
 */
@Entity(tableName = "simulation_results")
@TypeConverters(Converters::class)
data class SimulationResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val configId: Long,  // Foreign key to VentilationConfig
    
    // Velocity field (2D grid for simplified CFD)
    val gridWidth: Int,
    val gridHeight: Int,
    val velocityFieldX: String,  // JSON 2D array
    val velocityFieldY: String,  // JSON 2D array
    
    // Simulation parameters
    val resolution: Int = 128,
    val iterations: Int = 100,
    val timesteps: Int = 500,
    
    // Metrics
    val avgAirVelocity: Float,
    val maxAirVelocity: Float,
    val ventilationRate: Float,  // Air changes per hour (ACH)
    
    val deadZonePercentage: Float = 0f,
    val virusConcentrationField: String = "[]",
    val ventilationScore: Int = 0,
    
    val timestamp: Date
)

/**
 * Room TypeConverters for complex types
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromOpeningList(value: List<VentilationOpening>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toOpeningList(value: String): List<VentilationOpening> {
        val listType = object : TypeToken<List<VentilationOpening>>() {}.type
        return gson.fromJson(value, listType)
    }
}
