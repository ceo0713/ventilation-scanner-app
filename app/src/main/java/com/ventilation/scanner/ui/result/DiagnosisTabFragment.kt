package com.ventilation.scanner.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.ventilation.scanner.MainActivity
import com.ventilation.scanner.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiagnosisTabFragment : Fragment() {
    
    private lateinit var scoreValue: TextView
    private lateinit var scoreLabel: TextView
    private lateinit var scoreProgress: LinearProgressIndicator
    private lateinit var avgVelocityText: TextView
    private lateinit var maxVelocityText: TextView
    private lateinit var deadZoneText: TextView
    private lateinit var concentrationText: TextView
    private lateinit var recommendationsText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_diagnosis, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        scoreValue = view.findViewById(R.id.score_value)
        scoreLabel = view.findViewById(R.id.score_label)
        scoreProgress = view.findViewById(R.id.score_progress)
        avgVelocityText = view.findViewById(R.id.avg_velocity_text)
        maxVelocityText = view.findViewById(R.id.max_velocity_text)
        deadZoneText = view.findViewById(R.id.dead_zone_text)
        concentrationText = view.findViewById(R.id.concentration_text)
        recommendationsText = view.findViewById(R.id.recommendations_text)
        
        loadLatestResult()
    }
    
    override fun onResume() {
        super.onResume()
        loadLatestResult()
    }
    
    private fun loadLatestResult() {
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.simulationResultDao()
                    .getAllResults()
            }
            
            val latestResult = results.firstOrNull()
            
            latestResult?.let { result ->
                val score = result.ventilationScore
                scoreValue.text = score.toString()
                scoreProgress.progress = score
                
                val scoreGrade = when {
                    score >= 80 -> getString(R.string.score_excellent)
                    score >= 60 -> getString(R.string.score_good)
                    score >= 40 -> getString(R.string.score_fair)
                    else -> getString(R.string.score_poor)
                }
                scoreLabel.text = "${getString(R.string.ventilation_score)}: $scoreGrade"
                
                avgVelocityText.text = String.format("%.4f m/s", result.avgAirVelocity)
                maxVelocityText.text = String.format("%.4f m/s", result.maxAirVelocity)
                deadZoneText.text = String.format("%.1f%%", result.deadZonePercentage)
                concentrationText.text = "N/A"
                
                val recommendations = buildRecommendations(result)
                recommendationsText.text = recommendations
            } ?: run {
                scoreValue.text = "--"
                scoreLabel.text = getString(R.string.ventilation_score)
                scoreProgress.progress = 0
                avgVelocityText.text = "0.0 m/s"
                maxVelocityText.text = "0.0 m/s"
                deadZoneText.text = "0.0%"
                concentrationText.text = "0.0%"
                recommendationsText.text = "시뮬레이션을 실행하여 분석 결과를 확인하세요"
            }
        }
    }
    
    private fun buildRecommendations(result: com.ventilation.scanner.data.SimulationResult): String {
        val recommendations = mutableListOf<String>()
        
        if (result.deadZonePercentage > 20) {
            recommendations.add("⚠️ 데드존 비율이 높습니다 (${String.format("%.1f", result.deadZonePercentage)}%)")
            recommendations.add("• 환기구 위치 조정을 권장합니다")
        }
        
        if (result.avgAirVelocity < 0.1) {
            recommendations.add("⚠️ 평균 풍속이 낮습니다")
            recommendations.add("• 창문을 열어 자연 환기를 시도하세요")
        }
        
        if (result.ventilationScore < 60) {
            recommendations.add("💡 공기살균기 추가 배치를 권장합니다")
            recommendations.add("• 솔루션 탭에서 배치 제안을 확인하세요")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ 환기 상태가 양호합니다")
            recommendations.add("• 현재 설정을 유지하세요")
        }
        
        return recommendations.joinToString("\n")
    }
}
