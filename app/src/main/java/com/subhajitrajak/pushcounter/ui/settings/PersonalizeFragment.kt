package com.subhajitrajak.pushcounter.ui.settings

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentPersonalizeBinding
import com.subhajitrajak.pushcounter.ui.dashboard.WorkoutSetupDialog
import com.subhajitrajak.pushcounter.utils.Preferences

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

        val prefs = Preferences.getInstance(requireContext())

        // Restore saved values
        binding.showCameraCardSwitch.isChecked = prefs.isCameraCardEnabled()
        binding.counterFeedbackSwitch.isChecked = prefs.isCounterFeedbackEnabled()

        binding.repCount.text = prefs.getTotalReps().toString()
        val restTimeMs = prefs.getRestTime()
        var restMinutes = restTimeMs / 1000 / 60
        var restSeconds = (restTimeMs / 1000) % 60
        binding.restTime.text = String.format("%02d:%02d", restMinutes, restSeconds)

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            showCameraCardSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.setCameraCardEnabled(isChecked)
            }

            counterFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.setCounterFeedbackEnabled(isChecked)
            }

            repCountCard.setOnClickListener {
                val dialog = WorkoutSetupDialog(1)
                dialog.onStartClick = { totalReps, _ ->
                    dialog.binding.apply {
                        prefs.setTotalReps(totalReps)
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
                        prefs.setRestTime(restTimeMs)
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