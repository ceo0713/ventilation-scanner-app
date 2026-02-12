package com.ventilation.scanner.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
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

class ResultFragment : Fragment() {
    
    private lateinit var webView: WebView
    private lateinit var metricsText: TextView
    private lateinit var startSimulationButton: MaterialButton
    
    private var cfdSimulator: CFDSimulator? = null
    private val gson = Gson()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        webView = view.findViewById(R.id.webview)
        metricsText = view.findViewById(R.id.metrics_text)
        startSimulationButton = view.findViewById(R.id.start_simulation_button)
        
        cfdSimulator = CFDSimulator(requireContext())
        cfdSimulator?.getWebView()?.let { webView.addView(it) }
        
        startSimulationButton.setOnClickListener {
            runCompleteSimulation()
        }
    }
    
    private fun runCompleteSimulation() {
        lifecycleScope.launch {
            metricsText.text = "시뮬레이션 준비 중..."
            startSimulationButton.isEnabled = false
            
            val configs = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.ventilationConfigDao()
                    .getConfigsForScan(1)
            }
            
            val config = configs.firstOrNull() ?: run {
                metricsText.text = "설정이 없습니다. 설정 탭에서 환기구를 추가하세요"
                startSimulationButton.isEnabled = true
                return@launch
            }
            
            val scan = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.scanDao()
                    .getScanById(config.scanId)
            } ?: run {
                metricsText.text = "스캔 데이터를 찾을 수 없습니다"
                startSimulationButton.isEnabled = true
                return@launch
            }
            
            val verticesJson = scan.meshVertices
            val facesJson = scan.meshFaces
            
            val verticesType = object : com.google.gson.reflect.TypeToken<Array<List<Float>>>() {}.type
            val vertices = gson.fromJson<Array<List<Float>>>(verticesJson, verticesType)
                .map { it.toFloatArray() }
            val facesType = object : com.google.gson.reflect.TypeToken<Array<List<Int>>>() {}.type
            val faces = gson.fromJson<Array<List<Int>>>(facesJson, facesType)
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
            
            metricsText.text = "CFD 시뮬레이션 실행 중..."
            
            val results = cfdSimulator?.runSimulation(mesh, config, 128, 500)
            
            results?.let {
                val avgVel = it.avgVelocity
                val maxVel = it.maxVelocity
                
                metricsText.text = """
                    시뮬레이션 완료
                    평균 속도: ${String.format("%.4f", avgVel)} m/s
                    최대 속도: ${String.format("%.4f", maxVel)} m/s
                """.trimIndent()
                
                val simulationResult = SimulationResult(
                    configId = config.id,
                    gridWidth = it.gridWidth,
                    gridHeight = it.gridHeight,
                    velocityFieldX = gson.toJson(it.velocityField.ux),
                    velocityFieldY = gson.toJson(it.velocityField.uy),
                    avgAirVelocity = avgVel,
                    maxAirVelocity = maxVel,
                    ventilationRate = avgVel * 3600 / scan.height,
                    timestamp = Date()
                )
                
                withContext(Dispatchers.IO) {
                    (requireActivity() as MainActivity).database.simulationResultDao()
                        .insertResult(simulationResult)
                }
                
                Toast.makeText(requireContext(), "시뮬레이션 결과 저장 완료", Toast.LENGTH_SHORT).show()
            } ?: run {
                metricsText.text = "시뮬레이션 실패"
            }
            
            startSimulationButton.isEnabled = true
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webView.removeAllViews()
        webView.destroy()
    }
}
