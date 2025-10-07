package com.subhajitrajak.pushcounter.ui.settings

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.auth.GoogleAuthUiClient
import com.subhajitrajak.pushcounter.auth.UserData
import com.subhajitrajak.pushcounter.databinding.FragmentSettingsBinding
import com.subhajitrajak.pushcounter.utils.reminderUtils.PushupReminderManager
import com.subhajitrajak.pushcounter.utils.log
import kotlinx.coroutines.launch
import java.util.Calendar

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

        // Fetch signed-in user data
        val userData: UserData? = googleAuthUiClient.getSignedInUser()

        if (userData != null) {
            binding.userName.text = userData.username
            binding.userEmail.text = userData.userEmail

            Glide.with(requireContext())
                .load(userData.profilePictureUrl)
                .placeholder(R.drawable.person3)
                .into(binding.userImage)
        }

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            setReminder.setOnClickListener {
                // Show time picker
                val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                    .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                    .setTitleText("Select Reminder Time")
                    .build()

                // Show the picker
                picker.show(parentFragmentManager, "pushup_time_picker")

                // Handle the selected time
                picker.addOnPositiveButtonClickListener {
                    val selectedHour = picker.hour
                    val selectedMinute = picker.minute

                    // Schedule daily reminder
                    PushupReminderManager.scheduleDailyReminder(
                        requireContext(),
                        selectedHour,
                        selectedMinute
                    )
                }
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