package com.subhajitrajak.pushcounter.ui.dailyStats

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.databinding.FragmentDailyStatsBinding
import com.subhajitrajak.pushcounter.ui.shareStats.ShareStatsActivity
import com.subhajitrajak.pushcounter.utils.getMarker
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

        setupCharts()

        viewModel.loadDailyStats()
        viewModel.dailyStats.observe(viewLifecycleOwner) { stats ->
            adapter.submitList(stats)
        }

        // Attach chart model producer
        binding.chartView.modelProducer = viewModel.modelProducer

        // Observe LiveData
        viewModel.loadThisMonthPushups()
        viewModel.pushupCounts.observe(viewLifecycleOwner) { counts ->
            if (counts.isNotEmpty()) {
                // Optionally animate or refresh
                binding.chartView.invalidate()
            }
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

    private fun setupCharts() {
        val color = resources.getColor(R.color.primary, null)
        val lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(Fill(color)),
                areaFill = LineCartesianLayer.AreaFill.single(
                    Fill(
                        // gradient for chart fill
                        ShaderProvider.verticalGradient(
                            ColorUtils.setAlphaComponent(color, 102),
                            Color.TRANSPARENT
                        )
                    )
                ),
                pointConnector = LineCartesianLayer.PointConnector.cubic(0.5f)
            )
        )

        binding.chartView.apply {
            chart = chart!!.copy(
                (chart!!.layers[0] as LineCartesianLayer).copy(
                    lineProvider = lineProvider,
                    rangeProvider = CartesianLayerRangeProvider.auto()
                ),
                startAxis = (chart!!.startAxis as VerticalAxis).copy(
                    title = "Pushups Counts"
                ),
                bottomAxis = (chart!!.bottomAxis as HorizontalAxis).copy(
                    title = "Days of the month"
                ),
                marker = getMarker(requireContext())
            )

            this.modelProducer = modelProducer
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
        viewModel.loadThisMonthPushups()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}