package com.subhajitrajak.pushcounter.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentNotificationsBinding
import com.subhajitrajak.pushcounter.util.showToast

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        binding.notificationSwitch.isChecked = isGranted
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            handleBackButtonPress()
        }

        checkNotificationPermissionAndSetSwitch()
    }

    private fun checkNotificationPermissionAndSetSwitch() {
        binding.notificationSwitch.isChecked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isNotificationPermissionGranted()
        } else {
            true
        }

        val switch = binding.notificationSwitch

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnCheckedChangeListener
                } else {
                    showToast(
                        requireContext(),
                        "To fully disable notifications, turn it off in system settings."
                    )
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", requireContext().packageName, null)
                    startActivity(intent)
                }
            }
        } else {
            // Below Android 13, permission not needed
            switch.isChecked = true
            switch.isEnabled = false // Optional: dis`able switch for older versions
        }
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isNotificationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
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