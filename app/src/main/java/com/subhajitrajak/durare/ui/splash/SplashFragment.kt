package com.subhajitrajak.durare.ui.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.auth.GoogleAuthUiClient
import com.subhajitrajak.durare.databinding.FragmentSplashBinding
import com.subhajitrajak.durare.ui.weightSetup.WeightSetupViewModel
import com.subhajitrajak.durare.ui.weightSetup.WeightSetupViewModelFactory
import com.subhajitrajak.durare.utils.log
import kotlin.getValue

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightSetupViewModel by viewModels {
        WeightSetupViewModelFactory(requireContext())
    }

    private var isDataSaved = false

    private lateinit var googleAuthUiClient: GoogleAuthUiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Auth UI Client
        googleAuthUiClient = GoogleAuthUiClient(
            context = requireContext(),
            oneTapClient = Identity.getSignInClient(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkSavedData()
        viewModel.isDataSaved.observe(viewLifecycleOwner) { isDataSaved ->
            this.isDataSaved = isDataSaved
        }

        binding.lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                // slight delay before navigation to prevent overlap
                view.postDelayed({
                    // check if user is logged in
                    val isLoggedIn = googleAuthUiClient.isUserLoggedIn()
                    // navigate to the dashboard or on boarding screen based on the login status
                    navigateToNextScreen(isLoggedIn)
                }, 300)
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun navigateToNextScreen(isLoggedIn: Boolean) {
        try {
            val navController = findNavController()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()

            navController.navigate(
                if (isLoggedIn) {
                    if (isDataSaved) {
                        R.id.action_splashFragment_to_cameraAccessFragment
                    } else {
                        R.id.action_splashFragment_to_weightSetupFragment
                    }
                } else {
                    R.id.action_splashFragment_to_onBoardingFragment
                },
                null,
                navOptions
            )
        } catch (e: Exception) {
            log(e.message.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}