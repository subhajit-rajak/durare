package com.subhajitrajak.pushcounter.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.FragmentOnBoardingBinding
import com.subhajitrajak.pushcounter.ui.dashboard.DashboardFragment
import com.subhajitrajak.pushcounter.utils.log

class OnBoardingFragment : Fragment() {
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var onboardingScreens: List<OnboardingScreen>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onboardingScreens = listOf(
            OnboardingScreen(
                title = getString(R.string.onboarding_title_1),
                body = getString(R.string.onboarding_body_1),
                imageResId = R.drawable.onboarding_image_1
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
                imageResId = R.drawable.onboarding_image_4
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
        binding.skipButton.visibility = if (currentItem == onboardingScreens.size - 1) View.GONE else View.VISIBLE
    }

    private fun showGoogleSignInBottomSheet() {
        val bottomSheet = GoogleSignInBottomSheet()
        bottomSheet.onGoogleSignInClick = {
            navigateToDashboard()
        }
        bottomSheet.show(childFragmentManager, GoogleSignInBottomSheet.TAG)
    }

    private fun navigateToDashboard() {
        try {
            val navController = findNavController()
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.onBoardingFragment, true)
                .build()

            navController.navigate(
                R.id.action_onBoardingFragment_to_dashboardFragment,
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