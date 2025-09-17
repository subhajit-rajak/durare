package com.subhajitrajak.pushcounter.ui.dashboard

import android.animation.LayoutTransition
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.data.models.User
import com.subhajitrajak.pushcounter.databinding.FragmentDashboardBinding
import com.subhajitrajak.pushcounter.utils.log
import com.subhajitrajak.pushcounter.utils.removeWithAnim
import com.subhajitrajak.pushcounter.utils.showToast
import com.subhajitrajak.pushcounter.utils.showWithAnim50ms
import com.subhajitrajak.pushcounter.utils.showWithAnim
import java.time.LocalDate

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable layout transition (animates position changes)
        val transition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.APPEARING)
            enableTransitionType(LayoutTransition.CHANGE_APPEARING)

            // Optional: tweak timings for smooth ease-out
            setDuration(500)
            setInterpolator(LayoutTransition.APPEARING, DecelerateInterpolator())
            setInterpolator(LayoutTransition.CHANGE_APPEARING, DecelerateInterpolator())
        }
        binding.parentLinearLayout.layoutTransition = transition

        viewModel.dashboardStats.observe(viewLifecycleOwner) { stats ->
            // update UI
            binding.apply {
                todayText.text = stats.todayPushups.toString()
                last7Text.text = stats.last7Pushups.toString()
                last30Text.text = stats.last30Pushups.toString()
                lifetimeText.text = stats.lifetimePushups.toString()
                allUsersText.text = stats.allUsersTotal.toString()
            }

            binding.statsCard.showWithAnim()
            binding.globalCard.showWithAnim()
            binding.globalCardHeader.showWithAnim()
        }

        viewModel.thisMonthPushupCounts.observe(viewLifecycleOwner) { counts ->
            generateThisMonthHeatmap(counts)
        }

        viewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            binding.streakText.text = streak.first.toString()
            binding.currentStreakText.text = getString(R.string.current, streak.first)
            binding.highestStreakText.text = getString(R.string.highest, streak.second)
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            updateLeaderboard(users)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingIndicator.showWithAnim50ms()
            } else {
                binding.loadingIndicator.removeWithAnim()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            binding.loadingIndicator.removeWithAnim()
            if (errorMsg != null) {
                log(errorMsg)
                showToast(requireContext(), errorMsg)
            }
        }

        // Trigger loading
        viewModel.loadDashboardStats()
        viewModel.loadThisMonthPushupCounts()
        viewModel.loadCurrentStreak()
        viewModel.loadLeaderboard()

        binding.apply {

            settingsButton.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.loadDashboardStats()
                viewModel.loadThisMonthPushupCounts()
                viewModel.loadLeaderboard()
                viewModel.loadCurrentStreak()
                swipeRefresh.isRefreshing = false
            }

            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.primary)
            swipeRefresh.setColorSchemeResources(
                R.color.white,
                R.color.black
            )
        }
    }

    private fun updateLeaderboard(users: List<User>) {
        binding.apply {
            val positions = listOf(
                Triple(firstName, firstCount, firstImage),
                Triple(secondName, secondCount, secondImage),
                Triple(thirdName, thirdCount, thirdImage)
            )

            positions.zip(users.take(3)).forEach { (views, user) ->
                val (nameView, countView, imageView) = views

                nameView.text = user.userData.username
                countView.text = user.pushups.toString()

                Glide.with(requireContext())
                    .load(user.userData.profilePictureUrl)
                    .into(imageView)
            }

            leaderboardCardHeader.showWithAnim()
            leaderboardCard.showWithAnim()
        }
    }

    private fun generateThisMonthHeatmap(streaks: List<Int>) {
        val daysInMonth = streaks.size
        val maxValue = streaks.maxOrNull() ?: 0
        val levels = 5

        binding.heatmapLayout.removeAllViews()
        for (day in 1..daysInMonth) {
            val circleView = TextView(requireContext())

            val size = dpToPx(18)
            val params = FlexboxLayout.LayoutParams(size, size)
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            circleView.layoutParams = params

            circleView.text = ""
            circleView.gravity = Gravity.CENTER

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.day_circle)?.mutate()

            val value = streaks[day - 1]
            // determine level (0–4)
            val level = when {
                value == 0 -> 0
                maxValue == 0 -> 0
                else -> {
                    // scale contributions relative to maxValue
                    val ratio = value.toFloat() / maxValue
                    (ratio * (levels - 1)).toInt().coerceIn(1, levels - 1)
                }
            }

            // map level → color
            val colorRes = when (level) {
                0 -> R.color.level_0
                1 -> R.color.level_1
                2 -> R.color.level_2
                3 -> R.color.level_3
                else -> R.color.level_4
            }
            (drawable as GradientDrawable).setColor(requireContext().getColor(colorRes))

            val today = LocalDate.now().dayOfMonth
            if (day == today) {
                drawable.setStroke(dpToPx(1), requireContext().getColor(R.color.black))
            }

            circleView.background = drawable
            binding.heatmapLayout.addView(circleView)
        }

        binding.thisMonthCard.showWithAnim()
        binding.thisMonthCardHeader.showWithAnim()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.apply {
            loadDashboardStats()
            loadThisMonthPushupCounts()
            loadCurrentStreak()
            loadLeaderboard()
        }
    }
}