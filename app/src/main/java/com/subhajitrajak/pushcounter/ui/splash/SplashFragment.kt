package com.subhajitrajak.pushcounter.ui.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.auth.GoogleAuthUiClient
import com.subhajitrajak.pushcounter.databinding.FragmentSplashBinding
import com.subhajitrajak.pushcounter.utils.log

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

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

        binding.lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                // check if user is logged in
                val isLoggedIn = googleAuthUiClient.isUserLoggedIn()
                // navigate to the dashboard or on boarding screen based on the login status
                navigateToNextScreen(isLoggedIn)
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
                    R.id.action_splashFragment_to_dashboardFragment
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