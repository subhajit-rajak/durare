package com.subhajitrajak.durare.ui.reminder

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.durare.databinding.FragmentReminderBinding
import com.subhajitrajak.durare.utils.Preferences
import com.subhajitrajak.durare.utils.formatTwoDigits
import com.subhajitrajak.durare.utils.reminderUtils.PushupReminderManager
import com.subhajitrajak.durare.utils.showToast
import java.util.Locale

class ReminderFragment : Fragment() {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = Preferences.getInstance(requireContext())
        val hours = preferences.getReminderHour()
        val minutes = preferences.getReminderMinute()

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            reminderSwitch.isChecked = PushupReminderManager.isReminderSet(requireContext())

            npHours.apply {
                minValue = 0
                maxValue = 23
                value = hours
                wrapSelectorWheel = true
                setFormatter { it.formatTwoDigits()}
            }

            npMinutes.apply {
                minValue = 0
                maxValue = 59
                value = minutes
                wrapSelectorWheel = true
                setFormatter {
                    it.formatTwoDigits()
                }
            }

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            setReminder()
                        } else {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                requireContext().startActivity(intent)
                                showToast(requireContext(), "Please enable 'Alarms & reminders' permission for this app.")
                            } catch (e: Exception) {
                                showToast(requireContext(), "Unable to open settings.")
                            }
                        }
                    } else {
                        setReminder()
                    }
                } else {
                    PushupReminderManager.cancelReminder(requireContext())
                }
            }

            activateButton.setOnClickListener {
                setReminder()
            }
        }
    }

    private fun setReminder() {
        val selectedHour = binding.npHours.value
        val selectedMinute = binding.npMinutes.value

        PushupReminderManager.scheduleDailyReminder(
            requireContext(),
            selectedHour,
            selectedMinute
        )

        binding.reminderSwitch.isChecked = true
        val formattedTime = String.format(Locale.US,"%02d:%02d", selectedHour, selectedMinute)
        showToast(requireContext(), "Reminder set for $formattedTime")
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