package com.ventilation.scanner.cfd

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebSettings
import com.google.gson.Gson
import com.ventilation.scanner.data.VentilationConfig
import com.ventilation.scanner.data.VentilationOpening
import com.ventilation.scanner.data.OpeningType
import com.ventilation.scanner.arcore.SimpleMesh
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CFDSimulator(context: Context) {
    
    private val webView: WebView = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        addJavascriptInterface(JSBridge(), "AndroidBridge")
    }
    
    private val gson = Gson()
    private var simulationCallback: ((SimulationResults?) -> Unit)? = null
    
    init {
        webView.loadUrl("file:///android_asset/cfd-simulator.html")
    }
    
    suspend fun runSimulation(
        mesh: SimpleMesh,
        config: VentilationConfig,
        gridResolution: Int = 128,
        timesteps: Int = 500
    ): SimulationResults? = suspendCancellableCoroutine { continuation ->
        
        simulationCallback = { results ->
            continuation.resume(results)
        }
        
        val bounds = mesh.bounds
        val gridWidth = gridResolution
        val gridHeight = (gridResolution * (bounds.depth / bounds.width)).toInt()
        
        val obstacles = mutableListOf<Obstacle>()
        val inlets = mutableListOf<Inlet>()
        val outlets = mutableListOf<Outlet>()
        val acUnits = mutableListOf<ACDevice>()
        val sterilizers = mutableListOf<SterilizerDevice>()
        val ventilators = mutableListOf<VentilatorDevice>()
        val purifiers = mutableListOf<PurifierDevice>()
        
        val allDevices = config.inlets + config.outlets
        
        allDevices.forEach { device ->
            val gridX = ((device.x - bounds.minX) / bounds.width * gridWidth).toInt()
            val gridY = ((device.z - bounds.minZ) / bounds.depth * gridHeight).toInt()
            val gridW = (device.width / bounds.width * gridWidth).toInt().coerceAtLeast(1)
            val gridH = (device.height / bounds.depth * gridHeight).toInt().coerceAtLeast(1)
            val radius = (Math.sqrt(10.0) / bounds.width * gridWidth).toInt()
            
            when (device.type) {
                OpeningType.DOOR, OpeningType.WINDOW -> {
                    val vel = if (device.cmh > 0) {
                        device.cmh / (3600 * device.width * device.height)
                    } else {
                        device.velocity
                    }
                    inlets.add(Inlet(gridX, gridY, gridW, gridH, vel * 0.1f, 0f))
                }
                OpeningType.VENT -> {
                    outlets.add(Outlet(gridX, gridY, gridW, gridH))
                }
                OpeningType.AC_UNIT -> {
                    acUnits.add(ACDevice(gridX, gridY, device.cmh, gridW))
                }
                OpeningType.VENTILATOR -> {
                    ventilators.add(VentilatorDevice(gridX, gridY, device.cmh, 1f, 0f))
                }
                OpeningType.AIR_PURIFIER -> {
                    purifiers.add(PurifierDevice(gridX, gridY, device.cmh, radius))
                }
                OpeningType.AIR_STERILIZER -> {
                    sterilizers.add(SterilizerDevice(gridX, gridY, device.cmh, radius))
                }
            }
        }
        
        for (i in 0 until gridWidth) {
            obstacles.add(Obstacle(i, 0, 1, 1))
            obstacles.add(Obstacle(i, gridHeight - 1, 1, 1))
        }
        for (i in 0 until gridHeight) {
            obstacles.add(Obstacle(0, i, 1, 1))
            obstacles.add(Obstacle(gridWidth - 1, i, 1, 1))
        }
        
        val simConfig = SimulationConfig(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            obstacles = obstacles,
            inlets = inlets,
            outlets = outlets
        )
        
        val configJson = gson.toJson(simConfig)
        
        val acCalls = acUnits.joinToString("\n") { 
            "simulator.setACUnit(${it.x}, ${it.y}, ${it.cmh}, ${it.spread});" 
        }
        val sterilizerCalls = sterilizers.joinToString("\n") { 
            "simulator.setSterilizer(${it.x}, ${it.y}, ${it.cmh}, ${it.radius});" 
        }
        val ventilatorCalls = ventilators.joinToString("\n") { 
            "simulator.setVentilator(${it.x}, ${it.y}, ${it.cmh}, ${it.dirX}, ${it.dirY});" 
        }
        val purifierCalls = purifiers.joinToString("\n") { 
            "simulator.setPurifier(${it.x}, ${it.y}, ${it.cmh}, ${it.radius});" 
        }
        
        webView.post {
            webView.evaluateJavascript("""
                initSimulator($configJson);
                $acCalls
                $sterilizerCalls
                $ventilatorCalls
                $purifierCalls
                stepSimulation($timesteps);
                AndroidBridge.onSimulationComplete(JSON.stringify(simulator.getResults()));
            """.trimIndent(), null)
        }
    }
    
    fun getWebView(): WebView = webView
    
    suspend fun runBeforeAfterSimulation(
        mesh: SimpleMesh,
        beforeConfig: VentilationConfig,
        afterConfig: VentilationConfig,
        gridResolution: Int = 128,
        timesteps: Int = 500
    ): Pair<SimulationResults?, SimulationResults?> {
        val before = runSimulation(mesh, beforeConfig, gridResolution, timesteps)
        val after = runSimulation(mesh, afterConfig, gridResolution, timesteps)
        return Pair(before, after)
    }
    
    inner class JSBridge {
        @JavascriptInterface
        fun onSimulationComplete(resultsJson: String) {
            val results = try {
                gson.fromJson(resultsJson, SimulationResults::class.java)
            } catch (e: Exception) {
                null
            }
            
            simulationCallback?.invoke(results)
            simulationCallback = null
        }
    }
}

data class SimulationConfig(
    val gridWidth: Int,
    val gridHeight: Int,
    val obstacles: List<Obstacle>,
    val inlets: List<Inlet>,
    val outlets: List<Outlet>
)

data class Obstacle(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class Inlet(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val velocityX: Float,
    val velocityY: Float
)

data class Outlet(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class ACDevice(
    val x: Int,
    val y: Int,
    val cmh: Float,
    val spread: Int
)

data class SterilizerDevice(
    val x: Int,
    val y: Int,
    val cmh: Float,
    val radius: Int
)

data class VentilatorDevice(
    val x: Int,
    val y: Int,
    val cmh: Float,
    val dirX: Float,
    val dirY: Float
)

data class PurifierDevice(
    val x: Int,
    val y: Int,
    val cmh: Float,
    val radius: Int
)

data class DeadZoneResult(
    val map: List<Int>,
    val percentage: Float,
    val count: Int
)

data class SimulationResults(
    val velocityField: VelocityField,
    val avgVelocity: Float,
    val maxVelocity: Float,
    val gridWidth: Int,
    val gridHeight: Int,
    val deadZones: DeadZoneResult? = null,
    val deadZonePercentage: Float = 0f,
    val concentration: List<Float>? = null,
    val avgConcentration: Float = 1f
)

data class VelocityField(
    val ux: List<Float>,
    val uy: List<Float>,
    val density: List<Float>
)
