package com.subhajitrajak.pushcounter.ui.settings

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.DialogPermissionBinding
import com.subhajitrajak.pushcounter.databinding.FragmentPersonalizeBinding
import com.subhajitrajak.pushcounter.ui.dashboard.WorkoutSetupDialog
import com.subhajitrajak.pushcounter.utils.Preferences
import java.util.Locale

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val prefs = Preferences.getInstance(requireContext())

            // Restore saved values
            showCameraCardSwitch.isChecked = prefs.isCameraCardEnabled()
            soundFeedbackSwitch.isChecked = prefs.isSoundFeedbackEnabled()
            vibrationFeedbackSwitch.isChecked = prefs.isVibrationFeedbackEnabled()
            downThresholdSlider.value = prefs.getDownThreshold()
            upThresholdSlider.value = prefs.getUpThreshold()
            downThresholdValue.text = prefs.getDownThreshold().toInt().toString()
            upThresholdValue.text = prefs.getUpThreshold().toInt().toString()

            repCount.text = prefs.getTotalReps().toString()
            val restTimeMs = prefs.getRestTime()
            var restMinutes = restTimeMs / 1000 / 60
            var restSeconds = (restTimeMs / 1000) % 60
            restTime.text = String.format(Locale.US, "%02d:%02d", restMinutes, restSeconds)

            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            showCameraCardSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.setCameraCardEnabled(isChecked)
            }

            soundFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.setSoundFeedbackEnabled(isChecked)
            }

            vibrationFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.setVibrationFeedbackEnabled(isChecked)
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
                        restTime.text = String.format(Locale.US, "%02d:%02d", restMinutes, restSeconds)
                        dialog.dismiss()
                    }
                }
                dialog.show(childFragmentManager, WorkoutSetupDialog.TAG)
            }

            downThresholdSlider.addOnChangeListener { _, value, _ ->
                val threshold = value.coerceIn(20f, 70f)
                downThresholdValue.text = threshold.toInt().toString()
                prefs.setDownThreshold(threshold)
            }

            upThresholdSlider.addOnChangeListener { _, value, _ ->
                val threshold = value.coerceIn(10f, 50f)
                upThresholdValue.text = threshold.toInt().toString()
                prefs.setUpThreshold(threshold)
            }

            resetSettingsButton.setOnClickListener {
                showCustomDialog(
                    title = "Reset to defaults",
                    message = "Are you sure you want to reset to default values?",
                    positiveText = "Yes",
                    onPositiveClick = {
                        prefs.resetPersonalizationsToDefaults()
                        refreshPersonalizationUI(prefs)
                    }
                )
            }
        }
    }

    private fun refreshPersonalizationUI(prefs: Preferences) {
        binding.apply {
            showCameraCardSwitch.isChecked = prefs.isCameraCardEnabled()
            soundFeedbackSwitch.isChecked = prefs.isSoundFeedbackEnabled()
            vibrationFeedbackSwitch.isChecked = prefs.isVibrationFeedbackEnabled()
            downThresholdSlider.value = prefs.getDownThreshold()
            upThresholdSlider.value = prefs.getUpThreshold()
            downThresholdValue.text = prefs.getDownThreshold().toInt().toString()
            upThresholdValue.text = prefs.getUpThreshold().toInt().toString()
            repCount.text = prefs.getTotalReps().toString()

            val restTimeMs = prefs.getRestTime()
            val restMinutes = restTimeMs / 1000 / 60
            val restSeconds = (restTimeMs / 1000) % 60
            restTime.text = String.format(Locale.US, "%02d:%02d", restMinutes, restSeconds)
        }
    }

    private fun showCustomDialog(
        title: String,
        message: String,
        positiveText: String,
        negativeText: String = getString(R.string.cancel),
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit = {}
    ) {
        val dialogBinding = DialogPermissionBinding.inflate(layoutInflater)

        dialogBinding.dialogTitle.text = title
        dialogBinding.dialogMessage.text = message
        dialogBinding.dialogOk.text = positiveText
        dialogBinding.dialogCancel.text = negativeText

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.dialogCancel.setOnClickListener {
            dialog.dismiss()
            onNegativeClick()
        }

        dialogBinding.dialogOk.setOnClickListener {
            dialog.dismiss()
            onPositiveClick()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
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