package com.subhajitrajak.pushcounter.ui.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentPersonalizeBinding
import com.subhajitrajak.pushcounter.utils.Constants

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

        val prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        // Restore saved values
        binding.showCameraCardSwitch.isChecked = prefs.getBoolean(Constants.KEY_SHOW_CAMERA, false)
        binding.counterFeedbackSwitch.isChecked = prefs.getBoolean(Constants.KEY_COUNTER_FEEDBACK, false)

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            showCameraCardSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean(Constants.KEY_SHOW_CAMERA, isChecked) }
            }

            counterFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean(Constants.KEY_COUNTER_FEEDBACK, isChecked) }
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