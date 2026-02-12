package com.ventilation.scanner.ui.scan

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.ventilation.scanner.MainActivity
import com.ventilation.scanner.R
import com.ventilation.scanner.arcore.ARCoreManager
import com.ventilation.scanner.arcore.MeshGenerator
import com.ventilation.scanner.data.ScanData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import android.widget.TextView

class ScanFragment : Fragment() {
    
    private lateinit var textureView: TextureView
    private lateinit var statusText: TextView
    private lateinit var pointCountText: TextView
    private lateinit var startScanButton: MaterialButton
    private lateinit var saveScanButton: MaterialButton
    
    private var arCoreManager: ARCoreManager? = null
    private var isScanning = false
    private val gson = Gson()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        textureView = view.findViewById(R.id.texture_view)
        statusText = view.findViewById(R.id.status_text)
        pointCountText = view.findViewById(R.id.point_count_text)
        startScanButton = view.findViewById(R.id.start_scan_button)
        saveScanButton = view.findViewById(R.id.save_scan_button)
        
        startScanButton.setOnClickListener {
            toggleScanning()
        }
        
        saveScanButton.setOnClickListener {
            saveScan()
        }
        
        initializeARCore()
    }
    
    private fun initializeARCore() {
        lifecycleScope.launch {
            arCoreManager = ARCoreManager(requireContext())
            
            val success = withContext(Dispatchers.IO) {
                arCoreManager?.initializeSession() ?: false
            }
            
            if (success) {
                statusText.text = "ARCore 준비 완료"
                startScanButton.isEnabled = true
            } else {
                statusText.text = "ARCore를 사용할 수 없습니다"
                Toast.makeText(
                    requireContext(),
                    "3D 스캔 기능을 사용할 수 없습니다. 간단한 박스 모델로 진행합니다.",
                    Toast.LENGTH_LONG
                ).show()
                startScanButton.text = "간단 모델 생성"
                startScanButton.isEnabled = true
            }
        }
    }
    
    private fun toggleScanning() {
        if (isScanning) {
            stopScanning()
        } else {
            startScanning()
        }
    }
    
    private fun startScanning() {
        arCoreManager?.let { manager ->
            manager.resumeSession()
            isScanning = true
            startScanButton.text = getString(R.string.stop_scan)
            statusText.text = "스캔 중..."
            
            lifecycleScope.launch {
                while (isScanning) {
                    val frame = withContext(Dispatchers.IO) {
                        manager.captureFrame()
                    }
                    
                    frame?.let {
                        if (manager.isTracking(it)) {
                            withContext(Dispatchers.IO) {
                                manager.accumulatePoints(it)
                            }
                            pointCountText.text = "포인트: ${manager.getPointCount()}"
                        }
                    }
                    
                    kotlinx.coroutines.delay(33)
                }
            }
        } ?: run {
            createSimpleBox()
        }
    }
    
    private fun stopScanning() {
        isScanning = false
        arCoreManager?.pauseSession()
        startScanButton.text = getString(R.string.start_scan)
        statusText.text = "스캔 완료"
        saveScanButton.isEnabled = true
    }
    
    private fun createSimpleBox() {
        lifecycleScope.launch {
            val bounds = com.ventilation.scanner.arcore.BoundingBox(
                0f, 4f, 0f, 2.5f, 0f, 3f
            )
            val mesh = MeshGenerator.generateSimpleBoxMesh(bounds)
            
            val verticesJson = gson.toJson(mesh.vertices.map { it.toList() })
            val facesJson = gson.toJson(mesh.faces.map { it.toList() })
            
            val scanData = ScanData(
                name = "간단모델_${System.currentTimeMillis()}",
                timestamp = Date(),
                meshVertices = verticesJson,
                meshFaces = facesJson,
                minX = bounds.minX,
                maxX = bounds.maxX,
                minY = bounds.minY,
                maxY = bounds.maxY,
                minZ = bounds.minZ,
                maxZ = bounds.maxZ,
                width = bounds.width,
                height = bounds.height,
                depth = bounds.depth
            )
            
            val scanId = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.scanDao().insertScan(scanData)
            }
            
            pointCountText.text = "포인트: 8 (간단 모델)"
            statusText.text = "간단 모델 저장 완료 (ID: $scanId)"
            Toast.makeText(requireContext(), "간단 모델이 저장되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveScan() {
        lifecycleScope.launch {
            val points = arCoreManager?.getAccumulatedPoints() ?: return@launch
            
            if (points.isEmpty()) {
                createSimpleBox()
                return@launch
            }
            
            statusText.text = "메시 생성 중..."
            
            val mesh = withContext(Dispatchers.IO) {
                val voxelized = MeshGenerator.voxelizePoints(points, 0.05f)
                val downsampled = MeshGenerator.downsamplePoints(voxelized, 1000)
                MeshGenerator.generateConvexHullApprox(downsampled)
            }
            
            val verticesJson = gson.toJson(mesh.vertices.map { it.toList() })
            val facesJson = gson.toJson(mesh.faces.map { it.toList() })
            
            val scanData = ScanData(
                name = "스캔_${System.currentTimeMillis()}",
                timestamp = Date(),
                meshVertices = verticesJson,
                meshFaces = facesJson,
                minX = mesh.bounds.minX,
                maxX = mesh.bounds.maxX,
                minY = mesh.bounds.minY,
                maxY = mesh.bounds.maxY,
                minZ = mesh.bounds.minZ,
                maxZ = mesh.bounds.maxZ,
                width = mesh.bounds.width,
                height = mesh.bounds.height,
                depth = mesh.bounds.depth
            )
            
            val scanId = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.scanDao().insertScan(scanData)
            }
            
            statusText.text = "저장 완료 (ID: $scanId)"
            Toast.makeText(requireContext(), "스캔이 저장되었습니다", Toast.LENGTH_SHORT).show()
            
            arCoreManager?.clearAccumulatedPoints()
            pointCountText.text = "포인트: 0"
            saveScanButton.isEnabled = false
        }
    }
    
    override fun onPause() {
        super.onPause()
        arCoreManager?.pauseSession()
    }
    
    override fun onResume() {
        super.onResume()
        if (isScanning) {
            arCoreManager?.resumeSession()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        arCoreManager?.closeSession()
    }
}
