package com.subhajitrajak.durare.ui.pastConversations

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.auth.GoogleAuthUiClient
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.databinding.FragmentPastConversationsBinding

class PastConversationsFragment : Fragment() {
    private var _binding: FragmentPastConversationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConversationsViewModel by viewModels {
        ConversationsViewModelFactory(requireContext().applicationContext)
    }
    private lateinit var chatAdapter: ConversationsAdapter

    private val googleAuthUiClient: GoogleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext(),
            oneTapClient = Identity.getSignInClient(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPastConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userData: UserData? = googleAuthUiClient.getSignedInUser()

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            // setup chat adapter
            chatAdapter = ConversationsAdapter(requireContext(), userData?.profilePictureUrl)

            // setup layout manager
            val layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
                reverseLayout = false
            }

            chatRecyclerView.apply {
                adapter = chatAdapter
                setHasFixedSize(true)
                this.layoutManager = layoutManager
                layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.chat_anim)
                scheduleLayoutAnimation()
            }

            viewModel.chats.observe(viewLifecycleOwner) { chats ->
                chatAdapter.submitList(chats) {
                    chatRecyclerView.scheduleLayoutAnimation()
                }
            }
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

    override fun onResume() {
        super.onResume()
        viewModel.getAllConversations()
    }
}