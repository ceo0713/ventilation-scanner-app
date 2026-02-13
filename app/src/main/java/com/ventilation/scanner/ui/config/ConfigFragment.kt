package com.ventilation.scanner.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ventilation.scanner.MainActivity
import com.ventilation.scanner.R
import com.ventilation.scanner.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ConfigFragment : Fragment() {
    
    private lateinit var scanSpinner: Spinner
    private lateinit var openingsRecycler: RecyclerView
    private lateinit var devicesRecycler: RecyclerView
    private lateinit var runSimulationButton: MaterialButton
    
    private var selectedScanId: Long? = null
    private var selectedScan: ScanData? = null
    private val openings = mutableListOf<VentilationOpening>()
    private val devices = mutableListOf<VentilationOpening>()
    
    private lateinit var openingsAdapter: DeviceAdapter
    private lateinit var devicesAdapter: DeviceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        scanSpinner = view.findViewById(R.id.scan_spinner)
        openingsRecycler = view.findViewById(R.id.openings_recycler)
        devicesRecycler = view.findViewById(R.id.devices_recycler)
        runSimulationButton = view.findViewById(R.id.run_simulation_button)
        
        openingsAdapter = DeviceAdapter(
            onEdit = { device, index -> showEditBottomSheet(device, index, true) },
            onDelete = { index -> 
                openings.removeAt(index)
                openingsAdapter.submitList(openings.toList())
            }
        )
        
        devicesAdapter = DeviceAdapter(
            onEdit = { device, index -> showEditBottomSheet(device, index, false) },
            onDelete = { index -> 
                devices.removeAt(index)
                devicesAdapter.submitList(devices.toList())
            }
        )
        
        openingsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = openingsAdapter
            isNestedScrollingEnabled = false
        }
        
        devicesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
            isNestedScrollingEnabled = false
        }
        
        view.findViewById<MaterialButton>(R.id.add_opening_button).setOnClickListener {
            showAddBottomSheet(filterOpenings = true)
        }
        
        view.findViewById<MaterialButton>(R.id.add_device_button).setOnClickListener {
            showAddBottomSheet(filterOpenings = false)
        }
        
        runSimulationButton.setOnClickListener {
            saveConfig()
        }
        
        loadScans()
    }
    
    private fun loadScans() {
        lifecycleScope.launch {
            val scans = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.scanDao().getAllScans()
            }
            
            val scanNames = scans.map { "${it.name} (${it.width.toInt()}x${it.depth.toInt()}m)" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, scanNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            scanSpinner.adapter = adapter
            
            scanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val scan = scans.getOrNull(position)
                    selectedScanId = scan?.id
                    selectedScan = scan
                    loadDefaultOpenings(scan)
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedScanId = null
                    selectedScan = null
                }
            }
        }
    }
    
    private fun loadDefaultOpenings(scan: ScanData?) {
        scan?.let {
            openings.clear()
            openings.add(VentilationOpening(
                type = OpeningType.DOOR,
                name = "정문",
                x = it.minX + 0.5f,
                y = it.minY,
                z = it.minZ,
                width = 0.9f,
                height = 2.0f,
                velocity = 0.5f
            ))
            
            openingsAdapter.submitList(openings.toList())
            
            devices.clear()
            devicesAdapter.submitList(devices.toList())
        }
    }
    
    private fun showAddBottomSheet(filterOpenings: Boolean) {
        val sheet = DeviceConfigBottomSheet.newInstance(
            filterOpenings = filterOpenings
        )
        sheet.onDeviceSaved = { device, _ ->
            if (device.type.isOpening) {
                openings.add(device)
                openingsAdapter.submitList(openings.toList())
            } else {
                devices.add(device)
                devicesAdapter.submitList(devices.toList())
            }
        }
        sheet.show(childFragmentManager, "add_device")
    }
    
    private fun showEditBottomSheet(device: VentilationOpening, index: Int, isOpening: Boolean) {
        val sheet = DeviceConfigBottomSheet.newInstance(
            device = device,
            index = index,
            filterOpenings = isOpening
        )
        sheet.onDeviceSaved = { updated, idx ->
            if (isOpening) {
                openings[idx] = updated
                openingsAdapter.submitList(openings.toList())
            } else {
                devices[idx] = updated
                devicesAdapter.submitList(devices.toList())
            }
        }
        sheet.show(childFragmentManager, "edit_device")
    }
    
    private fun saveConfig() {
        val scanId = selectedScanId ?: run {
            Toast.makeText(requireContext(), "스캔을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val allDevices = openings + devices
            val inletDevices = allDevices.filter { 
                it.type.isOpening || it.type == OpeningType.AC_UNIT 
            }
            val outletDevices = allDevices.filter { 
                it.type == OpeningType.VENT || 
                it.type == OpeningType.VENTILATOR ||
                it.type == OpeningType.AIR_PURIFIER ||
                it.type == OpeningType.AIR_STERILIZER
            }
            
            val config = VentilationConfig(
                scanId = scanId,
                inlets = inletDevices,
                outlets = outletDevices,
                timestamp = Date()
            )
            
            withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database
                    .ventilationConfigDao().insertConfig(config)
            }
            
            Toast.makeText(
                requireContext(), 
                "설정 저장 완료. 분석 탭으로 이동하세요", 
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
