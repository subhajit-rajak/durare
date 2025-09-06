package com.subhajitrajak.pushcounter.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentPersonalizeBinding
import com.subhajitrajak.pushcounter.ui.dashboard.WorkoutSetupDialog
import com.subhajitrajak.pushcounter.utils.Constants.KEY_COUNTER_FEEDBACK
import com.subhajitrajak.pushcounter.utils.Constants.KEY_REST_TIME
import com.subhajitrajak.pushcounter.utils.Constants.KEY_SHOW_CAMERA
import com.subhajitrajak.pushcounter.utils.Constants.KEY_TOTAL_REPS
import com.subhajitrajak.pushcounter.utils.Constants.PREFS_NAME

class PersonalizeFragment : Fragment() {
    private var _binding: FragmentPersonalizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Restore saved values
        binding.showCameraCardSwitch.isChecked = prefs.getBoolean(KEY_SHOW_CAMERA, false)
        binding.counterFeedbackSwitch.isChecked = prefs.getBoolean(KEY_COUNTER_FEEDBACK, false)

        binding.repCount.text = prefs.getInt(KEY_TOTAL_REPS, 3).toString()
        val restTimeMs = prefs.getLong(KEY_REST_TIME, 0L).toString()
        var restMinutes = restTimeMs.toLong() / 1000 / 60
        var restSeconds = (restTimeMs.toLong() / 1000) % 60
        binding.restTime.text = String.format("%02d:%02d", restMinutes, restSeconds)

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            showCameraCardSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean(KEY_SHOW_CAMERA, isChecked) }
            }

            counterFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean(KEY_COUNTER_FEEDBACK, isChecked) }
            }

            repCountCard.setOnClickListener {
                val dialog = WorkoutSetupDialog(1)
                dialog.onStartClick = { totalReps, _ ->
                    dialog.binding.apply {
                        prefs.edit { putInt(KEY_TOTAL_REPS, totalReps) }
                        repCount.text = totalReps.toString()
                        dialog.dismiss()
                    }
                }
                dialog.show(childFragmentManager, WorkoutSetupDialog.TAG)
            }

            restTimeCard.setOnClickListener {
                val dialog = WorkoutSetupDialog(2)
                dialog.onStartClick = { _, restTimeMs ->
                    dialog.binding.apply {
                        prefs.edit { putLong(KEY_REST_TIME, restTimeMs) }
                        restMinutes = restTimeMs / 1000 / 60
                        restSeconds = (restTimeMs / 1000) % 60
                        restTime.text = String.format("%02d:%02d", restMinutes, restSeconds)
                        dialog.dismiss()
                    }
                }
                dialog.show(childFragmentManager, WorkoutSetupDialog.TAG)
            }
        }
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