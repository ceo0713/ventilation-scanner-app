package com.ventilation.scanner.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ventilation.scanner.R
import com.ventilation.scanner.data.OpeningType
import com.ventilation.scanner.data.VentilationOpening

class DeviceConfigBottomSheet : BottomSheetDialogFragment() {

    private var editingDevice: VentilationOpening? = null
    private var editingIndex: Int = -1
    private var filterOpenings: Boolean = false
    
    var onDeviceSaved: ((VentilationOpening, Int) -> Unit)? = null
    
    private lateinit var spinnerType: Spinner
    private lateinit var editName: TextInputEditText
    private lateinit var editX: TextInputEditText
    private lateinit var editY: TextInputEditText
    private lateinit var editZ: TextInputEditText
    private lateinit var editWidth: TextInputEditText
    private lateinit var editHeight: TextInputEditText
    private lateinit var editVelocity: TextInputEditText
    private lateinit var editCMH: TextInputEditText
    private lateinit var editTemperature: TextInputEditText
    private lateinit var editNotes: TextInputEditText
    private lateinit var cmhLayout: TextInputLayout
    private lateinit var temperatureLayout: TextInputLayout
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    
    private val deviceTypes: List<OpeningType>
        get() = if (filterOpenings) {
            listOf(OpeningType.DOOR, OpeningType.WINDOW, OpeningType.VENT)
        } else {
            listOf(
                OpeningType.AC_UNIT,
                OpeningType.VENTILATOR,
                OpeningType.AIR_PURIFIER,
                OpeningType.AIR_STERILIZER
            )
        }
    
    companion object {
        private const val ARG_DEVICE = "device"
        private const val ARG_INDEX = "index"
        private const val ARG_FILTER = "filter_openings"
        
        fun newInstance(
            device: VentilationOpening? = null,
            index: Int = -1,
            filterOpenings: Boolean = false
        ): DeviceConfigBottomSheet {
            return DeviceConfigBottomSheet().apply {
                arguments = Bundle().apply {
                    device?.let { putSerializable(ARG_DEVICE, it) }
                    putInt(ARG_INDEX, index)
                    putBoolean(ARG_FILTER, filterOpenings)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            editingDevice = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_DEVICE, VentilationOpening::class.java)
            } else {
                it.getSerializable(ARG_DEVICE) as? VentilationOpening
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_device_config, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        spinnerType = view.findViewById(R.id.spinner_type)
        editName = view.findViewById(R.id.edit_name)
        editX = view.findViewById(R.id.edit_x)
        editY = view.findViewById(R.id.edit_y)
        editZ = view.findViewById(R.id.edit_z)
        editWidth = view.findViewById(R.id.edit_width)
        editHeight = view.findViewById(R.id.edit_height)
        editVelocity = view.findViewById(R.id.edit_velocity)
        editCMH = view.findViewById(R.id.edit_cmh)
        editTemperature = view.findViewById(R.id.edit_temperature)
        editNotes = view.findViewById(R.id.edit_notes)
        cmhLayout = view.findViewById(R.id.cmh_layout)
        temperatureLayout = view.findViewById(R.id.temperature_layout)
        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
        
        setupTypeSpinner()
        setupConditionalFields()
        populateEditData()
        setupButtons()
    }
    
    private fun setupTypeSpinner() {
        val typeNames = deviceTypes.map { getKoreanName(it) }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            typeNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
        
        editingDevice?.let { device ->
            val index = deviceTypes.indexOf(device.type)
            if (index >= 0) {
                spinnerType.setSelection(index)
            }
        }
    }
    
    private fun setupConditionalFields() {
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = deviceTypes.getOrNull(position)
                updateConditionalFields(selectedType)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        updateConditionalFields(deviceTypes.firstOrNull())
    }
    
    private fun updateConditionalFields(type: OpeningType?) {
        type?.let {
            cmhLayout.visibility = if (it.isDevice) View.VISIBLE else View.GONE
            temperatureLayout.visibility = if (it == OpeningType.AC_UNIT) View.VISIBLE else View.GONE
            
            if (it.hasCMH && editCMH.text.isNullOrEmpty()) {
                editCMH.setText(it.defaultCMH.toInt().toString())
            }
        }
    }
    
    private fun populateEditData() {
        editingDevice?.let { device ->
            editName.setText(device.name)
            editX.setText(device.x.toString())
            editY.setText(device.y.toString())
            editZ.setText(device.z.toString())
            editWidth.setText(device.width.toString())
            editHeight.setText(device.height.toString())
            editVelocity.setText(device.velocity.toString())
            if (device.cmh > 0) {
                editCMH.setText(device.cmh.toInt().toString())
            }
            if (device.temperature != 0f) {
                editTemperature.setText(device.temperature.toInt().toString())
            }
            editNotes.setText(device.notes)
        } ?: run {
            editX.setText("0.0")
            editY.setText("0.0")
            editZ.setText("0.0")
            editWidth.setText("1.0")
            editHeight.setText("2.0")
            editVelocity.setText("0.5")
        }
    }
    
    private fun setupButtons() {
        btnSave.setOnClickListener {
            validateAndSave()?.let { device ->
                onDeviceSaved?.invoke(device, editingIndex)
                dismiss()
            }
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
    
    private fun validateAndSave(): VentilationOpening? {
        val selectedType = deviceTypes.getOrNull(spinnerType.selectedItemPosition)
            ?: return null.also { showError("장치 유형을 선택하세요") }
        
        val name = editName.text?.toString() ?: ""
        val finalName = name.ifEmpty { getKoreanName(selectedType) }
        
        val x = editX.text?.toString()?.toFloatOrNull()
            ?: return null.also { showError("X 위치를 입력하세요") }
        
        val y = editY.text?.toString()?.toFloatOrNull()
            ?: return null.also { showError("Y 위치를 입력하세요") }
        
        val z = editZ.text?.toString()?.toFloatOrNull()
            ?: return null.also { showError("Z 위치를 입력하세요") }
        
        val width = editWidth.text?.toString()?.toFloatOrNull()
            ?: return null.also { showError("너비를 입력하세요") }
        
        if (width <= 0) {
            return null.also { showError("너비는 0보다 커야 합니다") }
        }
        
        val height = editHeight.text?.toString()?.toFloatOrNull()
            ?: return null.also { showError("높이를 입력하세요") }
        
        if (height <= 0) {
            return null.also { showError("높이는 0보다 커야 합니다") }
        }
        
        val velocity = editVelocity.text?.toString()?.toFloatOrNull() ?: 0.5f
        
        val cmh = if (selectedType.hasCMH) {
            editCMH.text?.toString()?.toFloatOrNull()
                ?: return null.also { showError("풍량(CMH)을 입력하세요") }
        } else {
            0f
        }
        
        if (cmh < 0) {
            return null.also { showError("풍량은 0 이상이어야 합니다") }
        }
        
        val temperature = if (selectedType == OpeningType.AC_UNIT) {
            editTemperature.text?.toString()?.toFloatOrNull() ?: 0f
        } else {
            0f
        }
        
        val notes = editNotes.text?.toString() ?: ""
        
        return VentilationOpening(
            type = selectedType,
            name = finalName,
            x = x,
            y = y,
            z = z,
            width = width,
            height = height,
            openingDepth = 0.1f,
            velocity = velocity,
            cmh = cmh,
            temperature = temperature,
            notes = notes,
            isActive = true
        )
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun getKoreanName(type: OpeningType): String {
        return when (type) {
            OpeningType.DOOR -> "출입문"
            OpeningType.WINDOW -> "창문"
            OpeningType.VENT -> "환기구"
            OpeningType.AC_UNIT -> "에어컨"
            OpeningType.VENTILATOR -> "환기장치"
            OpeningType.AIR_PURIFIER -> "공기청정기"
            OpeningType.AIR_STERILIZER -> "공기살균기"
        }
    }
}
