package com.subhajitrajak.pushcounter.ui.onboarding

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.auth.GoogleAuthUiClient
import com.subhajitrajak.pushcounter.databinding.FragmentOnBoardingBinding
import com.subhajitrajak.pushcounter.utils.log
import com.subhajitrajak.pushcounter.utils.removeWithAnim
import com.subhajitrajak.pushcounter.utils.showToast
import com.subhajitrajak.pushcounter.utils.showWithAnim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnBoardingFragment : Fragment() {
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var onboardingScreens: List<OnboardingScreen>

    private lateinit var googleAuthUiClient: GoogleAuthUiClient
    private val signInViewModel: SignInViewModel by viewModels()

    // Activity result launcher for handling sign-in response
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                CoroutineScope(Dispatchers.Main).launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(intent)
                    signInViewModel.onSignInResult(signInResult)

                    if (signInResult.data != null) {
                        signInViewModel.saveUserData(signInResult.data).observe(viewLifecycleOwner,
                            Observer { result ->
                                result.onSuccess {
                                    val username = signInResult.data.username
                                    val firstName = username?.substring(0, username.indexOf(' '))
                                    showToast(requireContext(), "Welcome, $firstName")
                                    navigateToDashboard()
                                }
                                result.onFailure { e ->
                                    binding.loadingIndicator.removeWithAnim()
                                    showToast(requireContext(), "Sign-in failed: ${e.message}")
                                }
                            })
                    } else {
                        binding.loadingIndicator.removeWithAnim()
                        showToast(requireContext(), "Sign-in failed: ${signInResult.errorMessage}")
                    }
                }
            }
        } else {
            binding.loadingIndicator.removeWithAnim()
            showToast(requireContext(), "Sign-in cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Auth UI Client
        googleAuthUiClient = GoogleAuthUiClient(
            context = requireContext(),
            oneTapClient = Identity.getSignInClient(requireContext())
        )

        onboardingScreens = listOf(
            OnboardingScreen(
                title = getString(R.string.onboarding_title_1),
                body = getString(R.string.onboarding_body_1),
                imageResId = R.drawable.onboarding_image_1_1
            ),
            OnboardingScreen(
                title = getString(R.string.onboarding_title_2),
                body = getString(R.string.onboarding_body_2),
                imageResId = R.drawable.onboarding_image_2
            ),
            OnboardingScreen(
                title = getString(R.string.onboarding_title_3),
                body = getString(R.string.onboarding_body_3),
                imageResId = R.drawable.onboarding_image_3
            ),
            OnboardingScreen(
                title = getString(R.string.onboarding_title_4),
                body = getString(R.string.onboarding_body_4),
                imageResId = R.drawable.onboarding_image_4_1
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupNavigation()
        updateSkipButtonVisibility()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(onboardingScreens)
        binding.viewPager.adapter = onboardingAdapter
        
        // Connect dots indicator to ViewPager2
        binding.dotsIndicator.attachTo(binding.viewPager)
        
        // Listen to page changes to update UI
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateSkipButtonVisibility()
            }
        })
    }

    private fun setupNavigation() {
        // Skip button - navigate to last screen
        binding.skipButton.setOnClickListener {
            binding.viewPager.currentItem = onboardingScreens.size - 1
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if(position == onboardingScreens.size - 1) {
                    binding.nextImage.setImageResource(R.drawable.check)
                } else {
                    binding.nextImage.setImageResource(R.drawable.arrow_right)
                }
            }
        })

        // Next button
        binding.nextButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingScreens.size - 1) {
                // Move to next screen
                binding.viewPager.currentItem = currentItem + 1
            } else {
                // Show Google sign-in bottom sheet on last screen
                showGoogleSignInBottomSheet()
            }
        }
    }

    private fun updateSkipButtonVisibility() {
        val currentItem = binding.viewPager.currentItem
        // Hide skip button on last screen
        if (currentItem == onboardingScreens.size - 1) {
            binding.skipButton.removeWithAnim()
        } else {
            binding.skipButton.visibility = View.VISIBLE
        }
    }

    private fun showGoogleSignInBottomSheet() {
        val bottomSheet = GoogleSignInBottomSheet()
        bottomSheet.onGoogleSignInClick = {
            loginUsingGoogle()
        }
        bottomSheet.show(childFragmentManager, GoogleSignInBottomSheet.TAG)
    }

    // google login
    private fun loginUsingGoogle() {
        binding.loadingIndicator.showWithAnim(1000)
        CoroutineScope(Dispatchers.Main).launch {
            val intentSender = googleAuthUiClient.signIn()
            if (intentSender != null) {
                signInLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            } else {
                binding.loadingIndicator.removeWithAnim()
                showToast(requireContext(), "Google Sign-In failed")
            }
        }
    }

    private fun navigateToDashboard() {
        try {
            val navController = findNavController()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.onBoardingFragment, true)
                .build()

            navController.navigate(
                R.id.action_onBoardingFragment_to_cameraAccessFragment,
                null,
                navOptions
            )
        } catch (e: Exception) {
            log(e.message.toString())
        } finally {
            _binding?.loadingIndicator?.removeWithAnim()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}