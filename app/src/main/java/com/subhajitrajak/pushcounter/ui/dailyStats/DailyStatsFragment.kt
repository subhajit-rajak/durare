package com.subhajitrajak.pushcounter.ui.dailyStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.subhajitrajak.pushcounter.databinding.FragmentDailyStatsBinding
import com.subhajitrajak.pushcounter.utils.log
import com.subhajitrajak.pushcounter.utils.removeWithAnim
import com.subhajitrajak.pushcounter.utils.showToast
import com.subhajitrajak.pushcounter.utils.showWithAnim

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

        adapter = DailyStatsAdapter()
        binding.recordsRv.adapter = adapter

        viewModel.loadDailyStats()
        viewModel.dailyStats.observe(viewLifecycleOwner) { stats ->
            adapter.submitList(stats)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingIndicator.showWithAnim()
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

    override fun onResume() {
        super.onResume()
        viewModel.loadDailyStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}