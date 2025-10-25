package com.subhajitrajak.durare.ui.pastConversations

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.auth.GoogleAuthUiClient
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.databinding.DialogPermissionBinding
import com.subhajitrajak.durare.databinding.FragmentPastConversationsBinding
import com.subhajitrajak.durare.utils.hideWithAnim
import com.subhajitrajak.durare.utils.showWithAnim
import kotlinx.coroutines.launch

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

    private var hideScrollButtonRunnable: Runnable? = null

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

            scrollBottom.setOnClickListener {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }

            chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                    val totalItems = chatAdapter.itemCount

                    scrollBottom.removeCallbacks(hideScrollButtonRunnable)

                    // User is not at the bottom
                    if (lastVisibleItem < totalItems - 1) {
                        scrollBottom.showWithAnim(200)

                        // Cancel any previous hide tasks
                        scrollBottom.removeCallbacks(hideScrollButtonRunnable)

                        // Schedule it to hide after 3 seconds
                        hideScrollButtonRunnable = Runnable {
                            scrollBottom.hideWithAnim(200)
                        }
                        scrollBottom.postDelayed(hideScrollButtonRunnable, 3000)

                    } else {
                        // If user scrolled to bottom, hide immediately
                        scrollBottom.hideWithAnim(200)
                        // Cancel pending hide actions
                        scrollBottom.removeCallbacks(hideScrollButtonRunnable)
                    }
                }
            })

            moreOptions.setOnClickListener { view ->
                val popup = PopupMenu(requireContext(), view, Gravity.END, 0, R.style.CustomPopupMenu)
                popup.menuInflater.inflate(R.menu.menu_past_conversations, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_clear_chat -> {
                            showClearChatConfirmation()
                            true
                        }
                        R.id.info -> {
                            showInfoDialog()
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
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

    private fun showInfoDialog() {
        showCustomDialog(
            title = "Chat Privacy Information",
            message = "Your messages are stored only on your device and are never uploaded to any server. The AI processes your input to generate replies but does not keep your chats. Clearing app data or uninstalling will permanently delete your chat history.",
            positiveText = "Got it!",
            negativeText = ""
        )
    }

    private fun showClearChatConfirmation() {
        showCustomDialog(
            title = "Clear chat history?",
            message = "This will permanently delete all past conversations.",
            positiveText = "Clear",
            onPositiveClick = {
                lifecycleScope.launch {
                    viewModel.clearChat()
                }
            }
        )
    }

    private fun showCustomDialog(
        title: String,
        message: String,
        positiveText: String,
        negativeText: String = getString(R.string.cancel),
        onPositiveClick: () -> Unit = {},
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