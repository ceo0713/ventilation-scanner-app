package com.ventilation.scanner.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android:view.ViewGroup
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
    private lateinit var inletsRecycler: RecyclerView
    private lateinit var outletsRecycler: RecyclerView
    private lateinit var runSimulationButton: MaterialButton
    
    private var selectedScanId: Long? = null
    private val inlets = mutableListOf<VentilationOpening>()
    private val outlets = mutableListOf<VentilationOpening>()
    
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
        inletsRecycler = view.findViewById(R.id.inlets_recycler)
        outletsRecycler = view.findViewById(R.id.outlets_recycler)
        runSimulationButton = view.findViewById(R.id.run_simulation_button)
        
        inletsRecycler.layoutManager = LinearLayoutManager(requireContext())
        outletsRecycler.layoutManager = LinearLayoutManager(requireContext())
        
        view.findViewById<MaterialButton>(R.id.add_inlet_button).setOnClickListener {
            addInlet()
        }
        
        view.findViewById<MaterialButton>(R.id.add_outlet_button).setOnClickListener {
            addOutlet()
        }
        
        runSimulationButton.setOnClickListener {
            runSimulation()
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
                    selectedScanId = scans.getOrNull(position)?.id
                    loadDefaultOpenings(scans.getOrNull(position))
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedScanId = null
                }
            }
        }
    }
    
    private fun loadDefaultOpenings(scan: ScanData?) {
        scan?.let {
            inlets.clear()
            inlets.add(VentilationOpening(
                type = OpeningType.DOOR,
                x = it.minX + 0.5f,
                y = it.minY,
                z = it.minZ,
                width = 0.9f,
                height = 2.0f,
                velocity = 0.5f
            ))
            
            outlets.clear()
            outlets.add(VentilationOpening(
                type = OpeningType.WINDOW,
                x = it.maxX - 0.5f,
                y = it.maxY - 0.5f,
                z = it.maxZ,
                width = 1.0f,
                height = 1.0f,
                velocity = 0.3f
            ))
            
            updateRecyclerViews()
        }
    }
    
    private fun addInlet() {
        inlets.add(VentilationOpening(
            type = OpeningType.DOOR,
            x = 0f,
            y = 0f,
            z = 0f,
            width = 0.9f,
            height = 2.0f,
            velocity = 0.5f
        ))
        updateRecyclerViews()
    }
    
    private fun addOutlet() {
        outlets.add(VentilationOpening(
            type = OpeningType.VENT,
            x = 0f,
            y = 0f,
            z = 0f,
            width = 0.3f,
            height = 0.3f,
            velocity = 0.3f
        ))
        updateRecyclerViews()
    }
    
    private fun updateRecyclerViews() {
        inletsRecycler.adapter = SimpleAdapter(inlets)
        outletsRecycler.adapter = SimpleAdapter(outlets)
    }
    
    private fun runSimulation() {
        val scanId = selectedScanId ?: run {
            Toast.makeText(requireContext(), "스캔을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val config = VentilationConfig(
                scanId = scanId,
                inlets = inlets.toList(),
                outlets = outlets.toList(),
                timestamp = Date()
            )
            
            val configId = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.ventilationConfigDao().insertConfig(config)
            }
            
            Toast.makeText(requireContext(), "설정 저장 완료. 결과 탭으로 이동하세요", Toast.LENGTH_LONG).show()
        }
    }
    
    private class SimpleAdapter(private val items: List<VentilationOpening>) : 
        RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {
        
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textView.text = "${item.type} (${item.width}m x ${item.height}m) @ ${item.velocity}m/s"
        }
        
        override fun getItemCount() = items.size
    }
}
