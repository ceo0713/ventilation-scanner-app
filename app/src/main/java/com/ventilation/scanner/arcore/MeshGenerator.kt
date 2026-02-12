package com.ventilation.scanner.arcore

import kotlin.math.max
import kotlin.math.min

data class SimpleMesh(
    val vertices: List<FloatArray>,
    val faces: List<IntArray>,
    val bounds: BoundingBox
)

data class BoundingBox(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val minZ: Float,
    val maxZ: Float
) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    val depth: Float get() = maxZ - minZ
    val centerX: Float get() = (minX + maxX) / 2f
    val centerY: Float get() = (minY + maxY) / 2f
    val centerZ: Float get() = (minZ + maxZ) / 2f
}

object MeshGenerator {
    
    fun generateBoundingBox(points: List<FloatArray>): BoundingBox {
        if (points.isEmpty()) {
            return BoundingBox(0f, 0f, 0f, 0f, 0f, 0f)
        }
        
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = Float.MIN_VALUE
        
        for (point in points) {
            minX = min(minX, point[0])
            maxX = max(maxX, point[0])
            minY = min(minY, point[1])
            maxY = max(maxY, point[1])
            minZ = min(minZ, point[2])
            maxZ = max(maxZ, point[2])
        }
        
        return BoundingBox(minX, maxX, minY, maxY, minZ, maxZ)
    }
    
    fun generateSimpleBoxMesh(bounds: BoundingBox): SimpleMesh {
        val vertices = listOf(
            floatArrayOf(bounds.minX, bounds.minY, bounds.minZ),
            floatArrayOf(bounds.maxX, bounds.minY, bounds.minZ),
            floatArrayOf(bounds.maxX, bounds.maxY, bounds.minZ),
            floatArrayOf(bounds.minX, bounds.maxY, bounds.minZ),
            floatArrayOf(bounds.minX, bounds.minY, bounds.maxZ),
            floatArrayOf(bounds.maxX, bounds.minY, bounds.maxZ),
            floatArrayOf(bounds.maxX, bounds.maxY, bounds.maxZ),
            floatArrayOf(bounds.minX, bounds.maxY, bounds.maxZ)
        )
        
        val faces = listOf(
            intArrayOf(0, 1, 2), intArrayOf(0, 2, 3),
            intArrayOf(4, 5, 6), intArrayOf(4, 6, 7),
            intArrayOf(0, 1, 5), intArrayOf(0, 5, 4),
            intArrayOf(2, 3, 7), intArrayOf(2, 7, 6),
            intArrayOf(0, 3, 7), intArrayOf(0, 7, 4),
            intArrayOf(1, 2, 6), intArrayOf(1, 6, 5)
        )
        
        return SimpleMesh(vertices, faces, bounds)
    }
    
    fun downsamplePoints(points: List<FloatArray>, targetCount: Int): List<FloatArray> {
        if (points.size <= targetCount) return points
        
        val step = points.size / targetCount
        return points.filterIndexed { index, _ -> index % step == 0 }
    }
    
    fun voxelizePoints(points: List<FloatArray>, voxelSize: Float): List<FloatArray> {
        val voxelMap = mutableMapOf<Triple<Int, Int, Int>, FloatArray>()
        
        for (point in points) {
            val voxelX = (point[0] / voxelSize).toInt()
            val voxelY = (point[1] / voxelSize).toInt()
            val voxelZ = (point[2] / voxelSize).toInt()
            
            val key = Triple(voxelX, voxelY, voxelZ)
            if (!voxelMap.containsKey(key)) {
                voxelMap[key] = point
            }
        }
        
        return voxelMap.values.toList()
    }
    
    fun generateConvexHullApprox(points: List<FloatArray>): SimpleMesh {
        val bounds = generateBoundingBox(points)
        return generateSimpleBoxMesh(bounds)
    }
}
