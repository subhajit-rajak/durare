package com.subhajitrajak.pushcounter.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.DialogWorkoutSetupBinding
import com.subhajitrajak.pushcounter.utils.Constants.KEY_REST_TIME
import com.subhajitrajak.pushcounter.utils.Constants.KEY_TOTAL_REPS
import com.subhajitrajak.pushcounter.utils.Constants.PREFS_NAME
import com.subhajitrajak.pushcounter.utils.formatTwoDigits
import com.subhajitrajak.pushcounter.utils.showToast

class WorkoutSetupDialog(val flag: Int) : BottomSheetDialogFragment() {
    private var _binding: DialogWorkoutSetupBinding? = null
    val binding get() = _binding!!

    var onStartClick: ((totalReps: Int, restTimeMs: Long) -> Unit)? = null

    override fun getTheme(): Int = R.style.CustomBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWorkoutSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val totalReps = prefs.getInt(KEY_TOTAL_REPS, 3)
        val restTime = prefs.getLong(KEY_REST_TIME, 0)

        val restMinutes = (restTime / 1000 / 60).toInt()
        val restSeconds = ((restTime / 1000) % 60).toInt()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        if (flag == 1) { // setup total reps
            binding.restTimeTextView.visibility = View.GONE
            binding.restTimeInputLayout.visibility = View.GONE
            binding.btnStart.text = getString(R.string.apply)
        } else if (flag == 2) { // setup rest time
            binding.totalRepTextView.visibility = View.GONE
            binding.totalRepInputLayout.visibility = View.GONE
            binding.btnStart.text = getString(R.string.apply)
        }

        binding.npMinutes.apply {
            minValue = 0
            maxValue = 10
            value = restMinutes
            wrapSelectorWheel = true
            setFormatter {
                it.formatTwoDigits()
            }
        }

        binding.npSeconds.apply {
            minValue = 0
            maxValue = 59
            value = restSeconds
            wrapSelectorWheel = true
            setFormatter { it.formatTwoDigits()}
        }

        // Set default values
        binding.etTotalReps.setText(totalReps.toString())

        binding.btnStart.setOnClickListener {
            val totalRepsText = binding.etTotalReps.text.toString().trim()

            if (totalRepsText.isEmpty()) {
                showToast(requireContext(), getString(R.string.please_enter_total_reps))
                return@setOnClickListener
            }

            val totalReps = totalRepsText.toIntOrNull()
            if (totalReps == null || totalReps <= 0) {
                showToast(requireContext(), getString(R.string.please_enter_valid_reps))
                return@setOnClickListener
            }

            val restMinutes = binding.npMinutes.value
            val restSeconds = binding.npSeconds.value
            val totalRestTimeMs = (restMinutes * 60 + restSeconds) * 1000L

            onStartClick?.invoke(totalReps, totalRestTimeMs)
            dismiss()
        }

        binding.dismissButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WorkoutSetupBottomSheet"
    }
}