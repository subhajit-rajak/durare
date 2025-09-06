package com.subhajitrajak.pushcounter.ui.shareStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.FragmentShareStatsBinding

class ShareStatsFragment : Fragment() {
    private var _binding: FragmentShareStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pushUps = arguments?.getString("pushUps")?.toIntOrNull() ?: 0
        val time = arguments?.getString("time") ?: "0m 0s"
        val rest = arguments?.getString("rest") ?: "0m 0s"

        // setup viewpager
        val adapter = StatsPagerAdapter(this, pushUps, time, rest)
        binding.viewPager.adapter = adapter

        // setup dots indicator
        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(pushUps: String, time: String, rest: String): ShareStatsFragment {
            val fragment = ShareStatsFragment()
            val args = Bundle().apply {
                putString("pushUps", pushUps)
                putString("time", time)
                putString("rest", rest)
            }
            fragment.arguments = args
            return fragment
        }
    }
}