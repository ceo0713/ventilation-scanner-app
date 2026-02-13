package com.ventilation.scanner.ui.result

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ResultPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VisualizationTabFragment()
            1 -> DiagnosisTabFragment()
            2 -> SolutionTabFragment()
            else -> VisualizationTabFragment()
        }
    }
}
