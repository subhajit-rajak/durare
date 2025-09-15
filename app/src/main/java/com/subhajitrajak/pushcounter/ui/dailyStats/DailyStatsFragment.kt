package com.subhajitrajak.pushcounter.ui.dailyStats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.databinding.FragmentDailyStatsBinding
import com.subhajitrajak.pushcounter.ui.shareStats.ShareStatsActivity
import com.subhajitrajak.pushcounter.utils.log
import com.subhajitrajak.pushcounter.utils.removeWithAnim
import com.subhajitrajak.pushcounter.utils.showToast
import com.subhajitrajak.pushcounter.utils.showWithAnim50ms

class DailyStatsFragment : Fragment() {
    private var _binding: FragmentDailyStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DailyStatsViewModel by viewModels {
        DailyStatsViewModelFactory()
    }

    private lateinit var adapter: DailyStatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DailyStatsAdapter { stats ->
            binding.loadingIndicator.showWithAnim50ms()
            navigateToShareStats(stats)
        }
        binding.recordsRv.adapter = adapter

        viewModel.loadDailyStats()
        viewModel.dailyStats.observe(viewLifecycleOwner) { stats ->
            adapter.submitList(stats)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingIndicator.showWithAnim50ms()
            } else {
                binding.loadingIndicator.removeWithAnim()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            binding.loadingIndicator.removeWithAnim()
            if (errorMsg != null) {
                log(errorMsg)
                showToast(requireContext(), errorMsg)
            }
        }
    }

    // navigates to the share stats activity with the specified parameters
    private fun navigateToShareStats(stats: DailyPushStats) {
        val pushUps = stats.totalPushups.toString()
        val timeMinutes = stats.totalActiveTimeMs / 1000 / 60
        val timeSeconds = (stats.totalActiveTimeMs / 1000) % 60
        val time = "${timeMinutes}m ${timeSeconds}s"

        val restMinutes = stats.totalRestTimeMs / 1000 / 60
        val restSeconds = (stats.totalRestTimeMs / 1000) % 60
        val rest = "${restMinutes}m ${restSeconds}s"

        val intent = Intent(requireContext(), ShareStatsActivity::class.java).apply {
            putExtra(ShareStatsActivity.EXTRA_PUSH_UPS, pushUps)
            putExtra(ShareStatsActivity.EXTRA_TIME, time)
            putExtra(ShareStatsActivity.EXTRA_REST, rest)
        }
        startActivity(intent)
        binding.loadingIndicator.removeWithAnim()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDailyStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}