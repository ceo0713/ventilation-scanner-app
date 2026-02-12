package com.ventilation.scanner.cfd

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebSettings
import com.google.gson.Gson
import com.ventilation.scanner.data.VentilationConfig
import com.ventilation.scanner.data.VentilationOpening
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
        
        config.inlets.forEach { opening ->
            val gridX = ((opening.x - bounds.minX) / bounds.width * gridWidth).toInt()
            val gridY = ((opening.z - bounds.minZ) / bounds.depth * gridHeight).toInt()
            val gridW = (opening.width / bounds.width * gridWidth).toInt().coerceAtLeast(1)
            val gridH = (opening.height / bounds.depth * gridHeight).toInt().coerceAtLeast(1)
            
            inlets.add(Inlet(gridX, gridY, gridW, gridH, opening.velocity * 0.1f, 0f))
        }
        
        config.outlets.forEach { opening ->
            val gridX = ((opening.x - bounds.minX) / bounds.width * gridWidth).toInt()
            val gridY = ((opening.z - bounds.minZ) / bounds.depth * gridHeight).toInt()
            val gridW = (opening.width / bounds.width * gridWidth).toInt().coerceAtLeast(1)
            val gridH = (opening.height / bounds.depth * gridHeight).toInt().coerceAtLeast(1)
            
            outlets.add(Outlet(gridX, gridY, gridW, gridH))
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
        
        webView.post {
            webView.evaluateJavascript("""
                initSimulator($configJson);
                stepSimulation($timesteps);
                AndroidBridge.onSimulationComplete(JSON.stringify(getResults()));
            """.trimIndent(), null)
        }
    }
    
    fun getWebView(): WebView = webView
    
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

data class SimulationResults(
    val velocityField: VelocityField,
    val avgVelocity: Float,
    val maxVelocity: Float,
    val gridWidth: Int,
    val gridHeight: Int
)

data class VelocityField(
    val ux: List<Float>,
    val uy: List<Float>,
    val density: List<Float>
)
