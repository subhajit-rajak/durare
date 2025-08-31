package com.subhajitrajak.pushcounter.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            personalize.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_personalizeFragment)
            }

            notifications.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_notificationsFragment)
            }

            appearance.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_appearanceFragment)
            }

            accountInformation.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_accountFragment)
            }
        }

        setupAppVersions()
    }

    private fun setupAppVersions() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionName = packageInfo.versionName

        @Suppress("DEPRECATION")
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode
        }

        val text = "Version - $versionName ($versionCode)"
        binding.appVersion.text = text
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