package com.subhajitrajak.pushcounter.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.FragmentLeaderboardBinding
import com.subhajitrajak.pushcounter.ui.dashboard.DashboardViewModel
import com.subhajitrajak.pushcounter.ui.dashboard.DashboardViewModelFactory

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(requireContext().applicationContext)
    }
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = LeaderboardAdapter(requireContext())
        binding.leaderboard.adapter = adapter

        viewModel.leaderboard.observe(viewLifecycleOwner) { newUsers ->
            adapter.submitList(newUsers)

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val rank = newUsers.indexOfFirst { it.userData.userId == currentUserId } + 1

            if (rank > 0) {
                val currentUser = newUsers[rank - 1]

                binding.userRankText.text = getString(R.string.you_are_at_rank, rank)
                binding.rankComment.text = getRankComment(rank)
                binding.userRankScore.text = currentUser.pushups.toString()
            }
        }
    }

    private fun getRankComment(rank: Int): String {
        return when (rank) {
            1 -> "🏆 You’re on top!"
            in 2..5 -> "🔥 Almost there!"
            in 6..10 -> "💪 Keep pushing!"
            else -> "👏 Stay consistent!"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLeaderboard()
    }
}