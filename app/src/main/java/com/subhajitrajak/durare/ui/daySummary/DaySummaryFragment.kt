package com.subhajitrajak.durare.ui.daySummary

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.data.models.DailyPushStats
import com.subhajitrajak.durare.databinding.FragmentDaySummaryBinding
import com.subhajitrajak.durare.ui.shareStats.ShareStatsActivity
import com.subhajitrajak.durare.utils.getFormattedDate
import com.subhajitrajak.durare.utils.getFormattedTime
import java.util.Locale

class DaySummaryFragment : Fragment() {
    private var _binding: FragmentDaySummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var stats: DailyPushStats
    private var pushups: Int = 0
    private var activeTime: String = ""
    private var restTime: String = ""
    private var sessionTime: String = ""
    private var date: String = ""
    private var pace: String = ""
    private var ratio: String = ""

    companion object {
        const val STATS = "stats"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(STATS, DailyPushStats::class.java)?.let {
                stats = it
            }
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable<DailyPushStats>(STATS)?.let {
                stats = it
            }
        }

        calculateStats()

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            shareButton.setOnClickListener {
                navigateToShareStats(stats)
            }

            setupTextViews()
            setUpChart()
        }
    }

    private fun setUpChart() {
        val chart = binding.pieChart

        val entries = listOf(
            PieEntry(stats.totalRestTimeMs.toFloat(), "Rest"),
            PieEntry(stats.totalActiveTimeMs.toFloat(), "Active")
        )

        val colors = intArrayOf(
            requireContext().getColor(R.color.light_grey),
            requireContext().getColor(R.color.primary)
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors, 255)
            sliceSpace = 8f // More noticeable gap between slices
            selectionShift = 0f
            setDrawValues(false) // Hide numbers
            valueTextColor = Color.TRANSPARENT // Extra safe
            isHighlightEnabled = false // Prevent touch highlight effect
        }

        val typeface = ResourcesCompat.getFont(requireContext(), R.font.special_gothic_semiexpanded_bold)

        chart.apply {
            data = PieData(dataSet)

            setDrawEntryLabels(true)
            setEntryLabelTextSize(12f)
            setEntryLabelTypeface(typeface)
            setEntryLabelColor(requireContext().getColor(R.color.black))
            setUsePercentValues(false)
            isDrawHoleEnabled = false
            setTouchEnabled(false)

            description.isEnabled = false
            legend.isEnabled = false
            setTransparentCircleAlpha(0)

            animateY(1000, Easing.EaseOutBack)

            invalidate()
        }
    }

    private fun calculateStats() {
        date = stats.date.getFormattedDate()

        pushups = stats.totalPushups
        activeTime = stats.totalActiveTimeMs.getFormattedTime()
        restTime = stats.totalRestTimeMs.getFormattedTime()
        sessionTime = (stats.totalActiveTimeMs + stats.totalRestTimeMs).getFormattedTime()

        val activeTimeInMinutes = stats.totalActiveTimeMs / 1000.0 / 60.0
        pace = if (activeTimeInMinutes > 0) {
            String.format(Locale.US, "%.1f", stats.totalPushups / activeTimeInMinutes)
        } else {
            "N/A"
        }

        ratio = if (stats.totalRestTimeMs > 0) {
            val ratioValue = stats.totalActiveTimeMs.toDouble() / stats.totalRestTimeMs
            String.format(Locale.US, "%.1f:1", ratioValue)
        } else {
            "1:0"
        }
    }

    private fun setupTextViews() = with(binding) {
        dateTextView.text = date
        pushUpCountTextView.text = pushups.toString()
        activeTimeTextView.text = activeTime
        restTimeTextView.text = restTime
        paceTextView.text = pace
        setsTextView.text = stats.totalReps.toString()
        sessionTimeTextView.text = sessionTime
        ratioTextView.text = ratio
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
    }

    private fun handleBackButtonPress() {
        if (isAdded) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}