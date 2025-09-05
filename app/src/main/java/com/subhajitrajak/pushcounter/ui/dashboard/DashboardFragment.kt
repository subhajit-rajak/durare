package com.subhajitrajak.pushcounter.ui.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.FragmentDashboardBinding
import com.subhajitrajak.pushcounter.ui.counter.CounterActivity
import com.subhajitrajak.pushcounter.utils.Constants.KEY_REST_TIME
import com.subhajitrajak.pushcounter.utils.Constants.KEY_TOTAL_REPS
import com.subhajitrajak.pushcounter.utils.Constants.PREFS_NAME

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startButton.setOnClickListener {
            showWorkoutSetupDialog()
        }

        // Navigate to SettingsFragment
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        }

        // Generate heatmap
        generateSampleHeatmap()
    }

    private fun generateSampleHeatmap() {
        val daysInMonth = 30
        val streaks = (1..daysInMonth).map { (0..2).random() }

        for (day in 1..daysInMonth) {
            val circleView = TextView(requireContext())

            val size = dpToPx(18)
            val params = FlexboxLayout.LayoutParams(size, size)
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            circleView.layoutParams = params

            circleView.text = ""
            circleView.gravity = Gravity.CENTER

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.day_circle)?.mutate()
            val color = when (streaks[day - 1]) {
                0 -> requireContext().getColor(R.color.level_0)
                1 -> requireContext().getColor(R.color.level_1)
                else -> requireContext().getColor(R.color.level_2)
            }
            (drawable as GradientDrawable).setColor(color)

            circleView.background = drawable
            binding.heatmapLayout.addView(circleView)
        }
    }

    // opens the workout setup dialog to set custom rep count and rest time using time pickers
    private fun showWorkoutSetupDialog() {
        val dialog = WorkoutSetupDialog()
        dialog.onStartClick = { totalReps, restTimeMs ->
            dialog.binding.apply {
                // set preferences
                val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit { putInt(KEY_TOTAL_REPS, totalReps) }
                prefs.edit { putLong(KEY_REST_TIME, restTimeMs) }

                // Start the workout with the specified parameters
                startWorkout(totalReps, restTimeMs)
                dialog.dismiss()
            }
        }
        dialog.show(childFragmentManager, WorkoutSetupDialog.TAG)
    }

    // navigates to the counter activity with the specified parameters
    private fun startWorkout(totalReps: Int, restTimeMs: Long) {
        val intent = Intent(requireContext(), CounterActivity::class.java).apply {
            putExtra("totalReps", totalReps)
            putExtra("restTimeMs", restTimeMs)
        }
        startActivity(intent)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}