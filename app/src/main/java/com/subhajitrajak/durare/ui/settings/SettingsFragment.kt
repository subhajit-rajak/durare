package com.subhajitrajak.durare.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
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

    private lateinit var prefs: Preferences

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
        prefs = Preferences.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionButtons()
        setupReminders()
        setupGeneralSettings()
        setupSupportSettings()
        setupAppVersions()
    }

    private fun setupActionButtons() = with(binding) {
        backButton.setOnClickListener {
            handleBackButtonPress()
        }

        val appLink = "https://play.google.com/store/apps/details?id=com.subhajitrajak.durare"

        rateUs.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, appLink.toUri())
            startActivity(intent)
        }

        shareApp.setOnClickListener {
            val text = "Check out this app to track your pushups and be consistent:\n$appLink"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(Intent.createChooser(intent, "Share"))
        }

        logout.setOnClickListener {
            signOutUser()
        }
    }

    private fun setupReminders() = with(binding) {
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
    }

    private fun signOutUser() {
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

    private fun setupGeneralSettings() = with(binding) {
        accountInformation.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_accountFragment) }
        personalize.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_personalizeFragment) }
        appearance.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_appearanceFragment) }
        notifications.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_notificationsFragment) }

        val isDark = prefs.isDarkTheme()
        val themeText = if (isDark) getString(R.string.dark) else getString(R.string.light)
        currentThemeProfile.text = themeText
    }

    private fun setupSupportSettings() = with(binding) {
        val contact = "subhajitrajak.dev@gmail.com"
        contactUs.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, "mailto:$contact".toUri())
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_contact))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_contact))
            startActivity(Intent.createChooser(intent, "Contact Us"))
        }
        reportAnIssue.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, "mailto:$contact".toUri())
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_report))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_report))
            startActivity(Intent.createChooser(intent, "Contact Us"))
        }
        privacyPolicy.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_privacyPolicyFragment) }
        termsAndConditions.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_termsFragment) }
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
        if (!isAdded) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}