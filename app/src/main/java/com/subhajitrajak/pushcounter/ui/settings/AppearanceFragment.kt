package com.subhajitrajak.pushcounter.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentAppearanceBinding
import com.subhajitrajak.pushcounter.utils.Preferences
import com.subhajitrajak.pushcounter.utils.ThemeManager
import com.subhajitrajak.pushcounter.utils.ThemeSwitcher

class AppearanceFragment : Fragment() {
    private var _binding: FragmentAppearanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            // Load current theme and set checked state
            val prefs = Preferences.getInstance(requireContext())
            val isDark = prefs.isDarkTheme()
            lightMode.isChecked = !isDark
            darkMode.isChecked = isDark

            // Listener for Light mode
            lightMode.setOnClickListener {
                ThemeManager.setDarkMode(requireContext(), false)
                ThemeSwitcher.switchThemeWithAnimation(requireActivity(), false)
                lightMode.isChecked = true
                darkMode.isChecked = false
            }

            // Listener for Dark mode
            darkMode.setOnClickListener {
                ThemeManager.setDarkMode(requireContext(), true)
                ThemeSwitcher.switchThemeWithAnimation(requireActivity(), true)
                darkMode.isChecked = true
                lightMode.isChecked = false
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