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
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Preferences.getInstance(requireContext())
        setupUI()
        setupListeners()
    }

    private fun setupUI() = with(binding) {
        showCameraCardSwitch.isChecked = prefs.isCameraCardEnabled()
        soundFeedbackSwitch.isChecked = prefs.isSoundFeedbackEnabled()
        vibrationFeedbackSwitch.isChecked = prefs.isVibrationFeedbackEnabled()
        downThresholdSlider.value = prefs.getDownThreshold()
        upThresholdSlider.value = prefs.getUpThreshold()
        downThresholdValue.text = prefs.getDownThreshold().toInt().toString()
        upThresholdValue.text = prefs.getUpThreshold().toInt().toString()
        repCount.text = prefs.getTotalReps().toString()
        restTime.text = formatRestTime(prefs.getRestTime())
    }

    private fun formatRestTime(ms: Long): String {
        val minutes = ms / 1000 / 60
        val seconds = (ms / 1000) % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun setupListeners() = with(binding) {

        backButton.setOnClickListener { handleBackButtonPress() }

        // Switches
        showCameraCardSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setCameraCardEnabled(isChecked)
        }
        soundFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setSoundFeedbackEnabled(isChecked)
        }
        vibrationFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setVibrationFeedbackEnabled(isChecked)
        }

        // Reps and rest time setup
        repCountCard.setOnClickListener { showWorkoutDialog(1) }
        restTimeCard.setOnClickListener { showWorkoutDialog(2) }

        // Sliders
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

        // Reset
        resetSettingsButton.setOnClickListener {
            showCustomDialog(
                title = "Reset to defaults",
                message = "Are you sure you want to reset to default values?",
                positiveText = "Yes",
                onPositiveClick = {
                    prefs.resetPersonalizationsToDefaults()
                    setupUI()
                }
            )
        }
    }

    private fun showWorkoutDialog(type: Int) {
        val dialog = WorkoutSetupDialog(type)
        dialog.onStartClick = { totalReps, restTimeMs ->
            when (type) {
                1 -> {
                    prefs.setTotalReps(totalReps)
                    binding.repCount.text = totalReps.toString()
                }
                2 -> {
                    prefs.setRestTime(restTimeMs)
                    binding.restTime.text = formatRestTime(restTimeMs)
                }
            }
            dialog.dismiss()
        }
        dialog.show(childFragmentManager, WorkoutSetupDialog.TAG)
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