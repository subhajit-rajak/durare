package com.subhajitrajak.durare.ui.notificationAcess

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.databinding.DialogPermissionBinding
import com.subhajitrajak.durare.databinding.FragmentNotificationAccessBinding

class NotificationAccessFragment : Fragment() {
    private var _binding: FragmentNotificationAccessBinding? = null
    private val binding get() = _binding!!

    // Modern permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                proceedToNextStep()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionRationale()
                } else {
                    showGoToSettingsDialog()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationAccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (notificationPermissionGranted()) {
            // Already granted → no need to ask again
            proceedToNextStep()
        }

        binding.skipButton.setOnClickListener {
            proceedToNextStep()
        }

        binding.allowButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun notificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun proceedToNextStep() {
        findNavController().navigate(R.id.action_notificationAccessFragment_to_dashboardFragment)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationale() {
        showCustomDialog(
            title = getString(R.string.notification_permission_needed),
            message = getString(R.string.this_app_requires_notification_access_to_function_properly),
            positiveText = getString(R.string.ok),
            onPositiveClick = {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }

    private fun showGoToSettingsDialog() {
        showCustomDialog(
            title = getString(R.string.permission_required),
            message = getString(R.string.notification_permission_has_been_permanently_denied_please_enable_it_in_app_settings),
            positiveText = getString(R.string.go_to_settings),
            onPositiveClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireActivity().packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        )
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}