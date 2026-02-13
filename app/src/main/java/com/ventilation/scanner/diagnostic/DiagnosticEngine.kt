package com.ventilation.scanner.diagnostic

import com.ventilation.scanner.data.SimulationResult
import kotlin.math.sqrt

object DiagnosticEngine {
    
    fun calculateVentilationScore(avgVelocity: Float, deadZonePercentage: Float): Int {
        val velocityScore = (avgVelocity * 100).coerceIn(0f, 50f)
        val deadZoneScore = ((100 - deadZonePercentage) / 2).coerceIn(0f, 50f)
        return (velocityScore + deadZoneScore).toInt()
    }
    
    fun getScoreGrade(score: Int): String {
        return when {
            score >= 80 -> "우수"
            score >= 60 -> "양호"
            score >= 40 -> "보통"
            else -> "미흡"
        }
    }
    
    fun generateRecommendations(result: SimulationResult): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        if (result.deadZonePercentage > 20) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.WARNING,
                    title = "데드존 비율이 높습니다",
                    description = "환기 사각지대: ${String.format("%.1f", result.deadZonePercentage)}%",
                    action = "환기구 위치 조정을 권장합니다"
                )
            )
        }
        
        if (result.avgAirVelocity < 0.1) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.WARNING,
                    title = "평균 풍속이 낮습니다",
                    description = "현재 풍속: ${String.format("%.4f", result.avgAirVelocity)} m/s",
                    action = "창문을 열어 자연 환기를 시도하세요"
                )
            )
        }
        
        if (result.ventilationScore < 60) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.SUGGESTION,
                    title = "공기살균기 추가 배치 권장",
                    description = "환기 점수: ${result.ventilationScore}/100",
                    action = "솔루션 탭에서 배치 제안을 확인하세요"
                )
            )
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.SUCCESS,
                    title = "환기 상태가 양호합니다",
                    description = "현재 설정을 유지하세요",
                    action = null
                )
            )
        }
        
        return recommendations
    }
    
    fun calculateSterilizerCount(result: SimulationResult, roomArea: Float = 30f): Int {
        val deadZoneRatio = result.deadZonePercentage / 100f
        val score = result.ventilationScore
        
        val baseCount = when {
            score >= 80 -> 0
            score >= 60 -> 1
            deadZoneRatio > 0.3 -> 3
            deadZoneRatio > 0.2 -> 2
            else -> 1
        }
        
        val areaBasedCount = (roomArea / 10f).toInt().coerceAtLeast(1)
        
        return maxOf(baseCount, areaBasedCount).coerceAtMost(5)
    }
    
    fun suggestPlacementLocations(
        result: SimulationResult,
        count: Int,
        roomWidth: Float,
        roomDepth: Float
    ): List<PlacementSuggestion> {
        val suggestions = mutableListOf<PlacementSuggestion>()
        
        val gridX = sqrt(count.toFloat()).toInt().coerceAtLeast(1)
        val gridZ = (count + gridX - 1) / gridX
        
        for (i in 0 until count) {
            val row = i / gridX
            val col = i % gridX
            
            val x = (col + 1) * roomWidth / (gridX + 1)
            val z = (row + 1) * roomDepth / (gridZ + 1)
            
            suggestions.add(
                PlacementSuggestion(
                    position = i + 1,
                    x = x,
                    y = 1.5f,
                    z = z,
                    coverage = 10f,
                    reason = if (result.deadZonePercentage > 20) {
                        "데드존 해소"
                    } else {
                        "균등 분산"
                    }
                )
            )
        }
        
        return suggestions
    }
    
    data class Recommendation(
        val type: RecommendationType,
        val title: String,
        val description: String,
        val action: String?
    )
    
    enum class RecommendationType {
        SUCCESS,
        WARNING,
        SUGGESTION
    }
    
    data class PlacementSuggestion(
        val position: Int,
        val x: Float,
        val y: Float,
        val z: Float,
        val coverage: Float,
        val reason: String
    )
    
    data class BeforeAfterComparison(
        val beforeScore: Int,
        val afterScore: Int,
        val beforeDeadZone: Float,
        val afterDeadZone: Float,
        val improvementPercentage: Float
    )
    
    fun estimateAfterImprovement(
        beforeResult: SimulationResult,
        sterilizerCount: Int
    ): BeforeAfterComparison {
        val improvementFactor = 1f + (sterilizerCount * 0.15f)
        val deadZoneReduction = 0.4f * sterilizerCount
        
        val afterScore = (beforeResult.ventilationScore * improvementFactor)
            .coerceIn(0f, 100f).toInt()
        val afterDeadZone = (beforeResult.deadZonePercentage * (1 - deadZoneReduction))
            .coerceAtLeast(0f)
        
        val improvement = if (beforeResult.ventilationScore > 0) {
            ((afterScore - beforeResult.ventilationScore).toFloat() / 
                beforeResult.ventilationScore * 100)
        } else {
            100f
        }
        
        return BeforeAfterComparison(
            beforeScore = beforeResult.ventilationScore,
            afterScore = afterScore,
            beforeDeadZone = beforeResult.deadZonePercentage,
            afterDeadZone = afterDeadZone,
            improvementPercentage = improvement
        )
    }
}
