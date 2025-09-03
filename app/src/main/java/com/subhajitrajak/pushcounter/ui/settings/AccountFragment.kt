package com.subhajitrajak.pushcounter.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.auth.GoogleAuthUiClient
import com.subhajitrajak.pushcounter.auth.UserData
import com.subhajitrajak.pushcounter.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
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
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }
        }

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