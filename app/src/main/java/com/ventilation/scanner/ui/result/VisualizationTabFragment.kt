package com.ventilation.scanner.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ventilation.scanner.MainActivity
import com.ventilation.scanner.R
import com.ventilation.scanner.arcore.SimpleMesh
import com.ventilation.scanner.cfd.CFDSimulator
import com.ventilation.scanner.data.SimulationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class VisualizationTabFragment : Fragment() {
    
    private lateinit var webView: WebView
    private lateinit var chipVelocity: Chip
    private lateinit var chipDeadzone: Chip
    private lateinit var chipConcentration: Chip
    private lateinit var startSimulationButton: MaterialButton
    
    private var cfdSimulator: CFDSimulator? = null
    private val gson = Gson()
    private var currentResults: com.ventilation.scanner.cfd.SimulationResults? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_visualization, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        webView = view.findViewById(R.id.webview)
        chipVelocity = view.findViewById(R.id.chip_velocity)
        chipDeadzone = view.findViewById(R.id.chip_deadzone)
        chipConcentration = view.findViewById(R.id.chip_concentration)
        startSimulationButton = view.findViewById(R.id.start_simulation_button)
        
        // Initialize WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.loadUrl("file:///android_asset/three-viewer.html")
        
        cfdSimulator = CFDSimulator(requireContext())
        
        chipVelocity.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chipDeadzone.isChecked = false
                chipConcentration.isChecked = false
                updateVisualization("velocity")
            }
        }
        
        chipDeadzone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chipVelocity.isChecked = false
                chipConcentration.isChecked = false
                updateVisualization("deadzone")
            }
        }
        
        chipConcentration.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chipVelocity.isChecked = false
                chipDeadzone.isChecked = false
                updateVisualization("concentration")
            }
        }
        
        startSimulationButton.setOnClickListener {
            runCompleteSimulation()
        }
    }
    
    private fun updateVisualization(mode: String) {
        // Store current visualization mode - actual visualization triggered after simulation completes
        currentVisualizationMode = mode
    }
    
    private var currentVisualizationMode = "velocity"
    
    private fun runCompleteSimulation() {
        lifecycleScope.launch {
            startSimulationButton.isEnabled = false
            startSimulationButton.text = "시뮬레이션 실행 중..."
            
            val configs = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.ventilationConfigDao()
                    .getAllConfigs()
            }
            
            val config = configs.firstOrNull() ?: run {
                Toast.makeText(requireContext(), "설정이 없습니다. 설정 탭에서 장치를 추가하세요", Toast.LENGTH_SHORT).show()
                startSimulationButton.isEnabled = true
                startSimulationButton.text = getString(R.string.run_simulation)
                return@launch
            }
            
            val scan = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.scanDao()
                    .getScanById(config.scanId)
            } ?: run {
                Toast.makeText(requireContext(), "스캔 데이터를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                startSimulationButton.isEnabled = true
                startSimulationButton.text = getString(R.string.run_simulation)
                return@launch
            }
            
            val verticesType = object : TypeToken<Array<List<Float>>>() {}.type
            val vertices = gson.fromJson<Array<List<Float>>>(scan.meshVertices, verticesType)
                .map { it.toFloatArray() }
            val facesType = object : TypeToken<Array<List<Int>>>() {}.type
            val faces = gson.fromJson<Array<List<Int>>>(scan.meshFaces, facesType)
                .map { it.toIntArray() }
            
            val mesh = SimpleMesh(
                vertices = vertices,
                faces = faces,
                bounds = com.ventilation.scanner.arcore.BoundingBox(
                    scan.minX, scan.maxX,
                    scan.minY, scan.maxY,
                    scan.minZ, scan.maxZ
                )
            )
            
            val results = cfdSimulator?.runSimulation(mesh, config, 128, 500)
            
            results?.let {
                currentResults = it
                
                val simulationResult = SimulationResult(
                    configId = config.id,
                    gridWidth = it.gridWidth,
                    gridHeight = it.gridHeight,
                    velocityFieldX = gson.toJson(it.velocityField.ux),
                    velocityFieldY = gson.toJson(it.velocityField.uy),
                    avgAirVelocity = it.avgVelocity,
                    maxAirVelocity = it.maxVelocity,
                    ventilationRate = it.avgVelocity * 3600 / scan.height,
                    deadZonePercentage = it.deadZonePercentage,
                    virusConcentrationField = gson.toJson(it.concentration),
                    ventilationScore = calculateScore(it.avgVelocity, it.deadZonePercentage),
                    timestamp = Date()
                )
                
                withContext(Dispatchers.IO) {
                    (requireActivity() as MainActivity).database.simulationResultDao()
                        .insertResult(simulationResult)
                }
                
                visualizeResults(mesh, it, config)
                
                Toast.makeText(requireContext(), "시뮬레이션 완료", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "시뮬레이션 실패", Toast.LENGTH_SHORT).show()
            }
            
            startSimulationButton.isEnabled = true
            startSimulationButton.text = getString(R.string.run_simulation)
        }
    }
    
    private fun calculateScore(avgVelocity: Float, deadZonePercentage: Float): Int {
        val velocityScore = (avgVelocity * 100).coerceIn(0f, 50f)
        val deadZoneScore = ((100 - deadZonePercentage) / 2).coerceIn(0f, 50f)
        return (velocityScore + deadZoneScore).toInt()
    }
    
    private fun visualizeResults(mesh: SimpleMesh, results: com.ventilation.scanner.cfd.SimulationResults, config: com.ventilation.scanner.data.VentilationConfig) {
        if (!isAdded || isDetached) return
        
        val meshJson = """
            {
                "vertices": ${gson.toJson(mesh.vertices.map { it.toList() })},
                "faces": ${gson.toJson(mesh.faces.map { it.toList() })},
                "bounds": {
                    "minX": ${mesh.bounds.minX}, "maxX": ${mesh.bounds.maxX},
                    "minY": ${mesh.bounds.minY}, "maxY": ${mesh.bounds.maxY},
                    "minZ": ${mesh.bounds.minZ}, "maxZ": ${mesh.bounds.maxZ}
                }
            }
        """.trimIndent()
        
        webView.post {
            webView.evaluateJavascript("init();", null)
            webView.evaluateJavascript("loadRoom($meshJson);", null)
            
            val velocityJson = """
                {
                    "ux": ${gson.toJson(results.velocityField.ux)},
                    "uy": ${gson.toJson(results.velocityField.uy)},
                    "width": ${results.gridWidth},
                    "height": ${results.gridHeight}
                }
            """.trimIndent()
            
            when (currentVisualizationMode) {
                "velocity" -> {
                    webView.evaluateJavascript("visualizeAirflow($velocityJson);", null)
                }
                "deadzone" -> {
                    val deadZonesJson = gson.toJson(results.deadZones)
                    webView.evaluateJavascript("visualizeDeadZones($deadZonesJson, $velocityJson);", null)
                }
                "concentration" -> {
                    val concentrationJson = """
                        {
                            "data": ${gson.toJson(results.concentration)},
                            "width": ${results.gridWidth},
                            "height": ${results.gridHeight},
                            "avgConcentration": ${results.avgConcentration}
                        }
                    """.trimIndent()
                    webView.evaluateJavascript("visualizeConcentration($concentrationJson, $velocityJson);", null)
                }
            }
            
            val devicesJson = gson.toJson(config.inlets + config.outlets)
            webView.evaluateJavascript("addDeviceMarkers($devicesJson);", null)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
