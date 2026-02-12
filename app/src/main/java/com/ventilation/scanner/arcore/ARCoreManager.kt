package com.ventilation.scanner.arcore

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.*
import java.nio.FloatBuffer

class ARCoreManager(private val context: Context) {
    
    private var session: Session? = null
    private var depthEnabled = false
    
    private val pointCloud = mutableListOf<FloatArray>()
    
    fun initializeSession(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (!availability.isSupported) {
            return false
        }
        
        try {
            session = Session(context)
            
            val config = Config(session)
            config.depthMode = Config.DepthMode.AUTOMATIC
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            
            session?.configure(config)
            depthEnabled = config.depthMode != Config.DepthMode.DISABLED
            
            return true
        } catch (e: Exception) {
            when (e) {
                is UnavailableArcoreNotInstalledException,
                is UnavailableUserDeclinedInstallationException,
                is UnavailableApkTooOldException,
                is UnavailableSdkTooOldException,
                is UnavailableDeviceNotCompatibleException -> {
                    return false
                }
                else -> throw e
            }
        }
    }
    
    fun resumeSession() {
        session?.resume()
    }
    
    fun pauseSession() {
        session?.pause()
    }
    
    fun closeSession() {
        session?.close()
        session = null
    }
    
    fun captureFrame(): Frame? {
        val session = session ?: return null
        
        return try {
            session.update()
        } catch (e: Exception) {
            null
        }
    }
    
    fun isTracking(frame: Frame): Boolean {
        return frame.camera.trackingState == TrackingState.TRACKING
    }
    
    fun capturePointCloud(frame: Frame): List<FloatArray> {
        val points = mutableListOf<FloatArray>()
        
        frame.acquirePointCloud().use { pointCloud ->
            val buffer = pointCloud.points
            buffer.rewind()
            
            while (buffer.remaining() >= 4) {
                val x = buffer.float
                val y = buffer.float
                val z = buffer.float
                val confidence = buffer.float
                
                if (confidence > 0.5f) {
                    points.add(floatArrayOf(x, y, z))
                }
            }
        }
        
        return points
    }
    
    fun captureDepthImage(frame: Frame): FloatBuffer? {
        if (!depthEnabled) return null
        
        return try {
            frame.acquireDepthImage16Bits().use { depthImage ->
                val width = depthImage.width
                val height = depthImage.height
                val buffer = depthImage.planes[0].buffer
                
                val floatBuffer = FloatBuffer.allocate(width * height)
                buffer.rewind()
                
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val depthSample = buffer.short.toInt() and 0xFFFF
                        val depthMeters = depthSample / 1000.0f
                        floatBuffer.put(depthMeters)
                    }
                }
                
                floatBuffer.rewind()
                floatBuffer
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun accumulatePoints(frame: Frame) {
        val newPoints = capturePointCloud(frame)
        pointCloud.addAll(newPoints)
    }
    
    fun getAccumulatedPoints(): List<FloatArray> {
        return pointCloud.toList()
    }
    
    fun clearAccumulatedPoints() {
        pointCloud.clear()
    }
    
    fun getPointCount(): Int {
        return pointCloud.size
    }
}
