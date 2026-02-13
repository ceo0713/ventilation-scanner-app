package com.ventilation.scanner.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ventilation.scanner.MainActivity
import com.ventilation.scanner.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolutionTabFragment : Fragment() {
    
    private lateinit var ventilationRecommendation: TextView
    private lateinit var sterilizerRecommendation: TextView
    private lateinit var placementSuggestion: TextView
    private lateinit var placementChips: ChipGroup
    private lateinit var runBeforeAfterButton: MaterialButton
    private lateinit var comparisonCard: MaterialCardView
    private lateinit var beforeScore: TextView
    private lateinit var beforeDeadzone: TextView
    private lateinit var afterScore: TextView
    private lateinit var afterDeadzone: TextView
    private lateinit var improvementText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_solution, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ventilationRecommendation = view.findViewById(R.id.ventilation_recommendation)
        sterilizerRecommendation = view.findViewById(R.id.sterilizer_recommendation)
        placementSuggestion = view.findViewById(R.id.placement_suggestion)
        placementChips = view.findViewById(R.id.placement_chips)
        runBeforeAfterButton = view.findViewById(R.id.run_before_after_button)
        comparisonCard = view.findViewById(R.id.comparison_card)
        beforeScore = view.findViewById(R.id.before_score)
        beforeDeadzone = view.findViewById(R.id.before_deadzone)
        afterScore = view.findViewById(R.id.after_score)
        afterDeadzone = view.findViewById(R.id.after_deadzone)
        improvementText = view.findViewById(R.id.improvement_text)
        
        loadSuggestions()
        
        runBeforeAfterButton.setOnClickListener {
            runBeforeAfterComparison()
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadSuggestions()
    }
    
    private fun loadSuggestions() {
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.simulationResultDao()
                    .getAllResults()
            }
            
            val latestResult = results.firstOrNull()
            
            latestResult?.let { result ->
                if (result.avgAirVelocity < 0.1) {
                    ventilationRecommendation.text = "⚠️ ${getString(R.string.open_ventilation)}"
                } else {
                    ventilationRecommendation.text = "✅ 자연 환기 상태 양호"
                }
                
                val sterilizerCount = calculateSterilizerCount(result)
                sterilizerRecommendation.text = getString(R.string.suggest_count, sterilizerCount)
                
                if (result.deadZonePercentage > 20) {
                    placementSuggestion.text = getString(R.string.dead_zone_warning, 
                        String.format("%.1f", result.deadZonePercentage))
                } else {
                    placementSuggestion.text = "환기 상태가 양호합니다"
                }
                
                placementChips.removeAllViews()
                for (i in 1..sterilizerCount) {
                    val chip = Chip(requireContext()).apply {
                        text = "위치 $i 제안"
                        isClickable = true
                        isCheckable = false
                        setChipIconResource(android.R.drawable.ic_menu_mylocation)
                    }
                    placementChips.addView(chip)
                }
            } ?: run {
                ventilationRecommendation.text = "시뮬레이션을 실행하여 분석 결과를 확인하세요"
                sterilizerRecommendation.text = "분석 필요"
                placementSuggestion.text = ""
                placementChips.removeAllViews()
            }
        }
    }
    
    private fun calculateSterilizerCount(result: com.ventilation.scanner.data.SimulationResult): Int {
        val deadZoneRatio = result.deadZonePercentage / 100f
        val score = result.ventilationScore
        
        return when {
            score >= 80 -> 0
            score >= 60 -> 1
            deadZoneRatio > 0.3 -> 3
            deadZoneRatio > 0.2 -> 2
            else -> 1
        }
    }
    
    private fun runBeforeAfterComparison() {
        lifecycleScope.launch {
            runBeforeAfterButton.isEnabled = false
            runBeforeAfterButton.text = "비교 실행 중..."
            
            val results = withContext(Dispatchers.IO) {
                (requireActivity() as MainActivity).database.simulationResultDao()
                    .getAllResults()
            }
            
            val latestResult = results.firstOrNull()
            
            latestResult?.let { before ->
                val afterScoreEstimate = (before.ventilationScore * 1.3f).coerceIn(0f, 100f).toInt()
                val afterDeadzoneEstimate = (before.deadZonePercentage * 0.6f).coerceAtMost(100f)
                
                beforeScore.text = "점수: ${before.ventilationScore}"
                beforeDeadzone.text = "데드존: ${String.format("%.1f", before.deadZonePercentage)}%"
                
                afterScore.text = "점수: $afterScoreEstimate"
                afterDeadzone.text = "데드존: ${String.format("%.1f", afterDeadzoneEstimate)}%"
                
                val improvement = if (before.ventilationScore > 0) {
                    ((afterScoreEstimate - before.ventilationScore) / before.ventilationScore.toFloat() * 100)
                } else {
                    100f
                }
                improvementText.text = "개선율: ${String.format("%.1f", improvement)}%"
                
                comparisonCard.visibility = View.VISIBLE
                
                Toast.makeText(requireContext(), "Before/After 비교 완료", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "시뮬레이션 결과가 없습니다", Toast.LENGTH_SHORT).show()
            }
            
            runBeforeAfterButton.isEnabled = true
            runBeforeAfterButton.text = getString(R.string.run_before_after)
        }
    }
}
