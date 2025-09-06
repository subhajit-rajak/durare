package com.subhajitrajak.pushcounter.ui.shareStats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatsPagerAdapter(
    activity: ShareStatsActivity,
    private val pushUps: String,
    private val time: String,
    private val rest: String
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VerticalStatsFragment.newInstance(pushUps, time, rest)
            1 -> HorizontalStatsFragment.newInstance(pushUps, time, rest)
            else -> VerticalStatsFragment.newInstance(pushUps, time, rest)
        }
    }
}