package com.subhajitrajak.durare.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.auth.GoogleAuthUiClient
import com.subhajitrajak.durare.databinding.FragmentSettingsBinding
import com.subhajitrajak.durare.utils.Preferences
import com.subhajitrajak.durare.utils.log
import com.subhajitrajak.durare.utils.reminderUtils.PushupReminderManager
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val googleAuthUiClient: GoogleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext(),
            oneTapClient = Identity.getSignInClient(requireContext())
        )
    }

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

            val isReminderSet = PushupReminderManager.isReminderSet(requireContext())

            if(isReminderSet) {
                val prefs = Preferences.getInstance(requireContext())
                val reminderHour = prefs.getReminderHour()
                val reminderMinute = prefs.getReminderMinute()
                val reminderText = String.format(Locale.US,"%02d:%02d", reminderHour, reminderMinute)
                timer.text = reminderText
            }

            setReminder.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_reminderFragment)
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

            logout.setOnClickListener {
                lifecycleScope.launch {
                    googleAuthUiClient.signOut()
                    try {
                        val navController = findNavController()
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.settingsFragment, true)
                            .build()

                        navController.navigate(
                            R.id.action_settingsFragment_to_onBoardingFragment,
                            null,
                            navOptions
                        )
                    } catch (e: Exception) {
                        log(e.message.toString())
                    }
                }
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