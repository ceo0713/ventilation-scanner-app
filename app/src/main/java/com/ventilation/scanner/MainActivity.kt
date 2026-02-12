package com.ventilation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ventilation.scanner.data.AppDatabase

class MainActivity : AppCompatActivity() {
    
    private val CAMERA_PERMISSION_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    
    lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        database = AppDatabase.getDatabase(applicationContext)
        
        if (!isARCoreSupportedAndUpToDate()) {
            Toast.makeText(
                this,
                "이 기기는 ARCore를 지원하지 않습니다. 3D 스캔 기능이 제한될 수 있습니다.",
                Toast.LENGTH_LONG
            ).show()
        }
        
        if (!hasCameraPermission()) {
            requestCameraPermission()
        }
        
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)
    }
    
    private fun hasCameraPermission(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            CAMERA_PERMISSION_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "카메라 권한이 허용되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "카메라 권한이 필요합니다. 3D 스캔을 사용할 수 없습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun isARCoreSupportedAndUpToDate(): Boolean {
        return try {
            val availability = com.google.ar.core.ArCoreApk.getInstance()
                .checkAvailability(applicationContext)
            
            availability.isSupported
        } catch (e: Exception) {
            false
        }
    }
}
