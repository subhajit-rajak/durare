package com.subhajitrajak.durare.ui.askAi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.auth.GoogleAuthUiClient
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.data.models.AiUserStats
import com.subhajitrajak.durare.data.repositories.AiChatRepository
import com.subhajitrajak.durare.databinding.FragmentAskAiBinding
import com.subhajitrajak.durare.utils.remove
import com.subhajitrajak.durare.utils.show
import com.subhajitrajak.durare.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

class AskAiFragment : Fragment() {
    private var _binding: FragmentAskAiBinding? = null
    private val binding get() = _binding!!

    private lateinit var aiStats: AiUserStats

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    private val requestMicPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startListening()
        } else {
            showToast(requireContext(), "Microphone permission is required to use speech-to-text")
        }
    }

    private lateinit var chatAdapter: AiChatAdapter
    private val viewModel: AiChatViewModel by viewModels {
        AiChatViewModelFactory(AiChatRepository(requireContext()))
    }

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
        _binding = FragmentAskAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(0, 0, 0, max(imeInsets.bottom, navInsets.bottom))
            insets
        }

        arguments?.getParcelable<AiUserStats>("ai_stats")?.let {
            aiStats = it
        }

        viewModel.setModel("gemini-2.5-flash-lite")

        binding.apply {
            backButton.setOnClickListener {
                handleBackButtonPress()
            }

            val userData: UserData? = googleAuthUiClient.getSignedInUser()

            // setup chat adapter
            chatAdapter = AiChatAdapter(requireContext(), mutableListOf(), userData?.profilePictureUrl)

            // setup layout manager
            val layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
                reverseLayout = false
            }
            chatRecyclerView.apply {
                adapter = chatAdapter
                setHasFixedSize(true)
                this.layoutManager = layoutManager
                itemAnimator = null
            }

            // setup send button
            sendButton.setOnClickListener {
                val message = messageEditText.text.toString().trim()
                if (message.isEmpty()) return@setOnClickListener

                // add user message to RecyclerView
                chatAdapter.addMessage(ChatMessage(message, true))
                messageEditText.text?.clear()
                chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

                // add "thinking..." placeholder
                chatAdapter.addMessage(ChatMessage("Thinking...", false))
                chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

                // launch ai request
                viewModel.askAI(
                    prompt = message,
                    userData = getUserPushupSummary(),
                    useStats = statsChip.isChecked,
                    remember = rememberChip.isChecked
                )
            }

            viewModel.response.observe(viewLifecycleOwner) { response ->
                // removes the thinking message
                chatAdapter.removeLastMessage()
                // add empty AI message first
                val aiMessage = ChatMessage("", false)
                chatAdapter.addMessage(aiMessage)
                val aiPosition = chatAdapter.itemCount - 1

                // Animate typing
                animateAiResponse(aiPosition, response)
            }
        }

        setupSpeechRecognition()
    }

    private fun animateAiResponse(position: Int, fullText: String) {
        val typingSpeed = 25L // ms per word or char (tweak for speed)
        val markwon = io.noties.markwon.Markwon.create(requireContext())

        val textBuffer = StringBuilder()
        val words = fullText.split(" ") // or fullText.toCharArray() for per-character typing

        viewLifecycleOwner.lifecycleScope.launch {
            for (word in words) {
                textBuffer.append(word).append(" ")
                val markdown = markwon.toMarkdown(textBuffer.toString())

                // Update adapter on main thread
                withContext(Dispatchers.Main) {
                    chatAdapter.updateAiMessage(position, markdown)
                    binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                }

                delay(typingSpeed)
            }
        }
    }

    fun getUserPushupSummary(): String {
        return """
        Total Pushups: ${aiStats.totalPushups}
        Average per day (last 30 days): ${String.format(Locale.US, "%.1f", aiStats.averagePerDay)}}
        Current Streak: ${aiStats.currentStreak} days
        Highest Streak: ${aiStats.highestStreak} days
        Last 7 days: ${aiStats.last7Days}
        Last 30 days: ${aiStats.last30Days}
    """.trimIndent()
    }

    private fun hideListeningOverlay() {
        binding.lottieAnimationView.remove()
        binding.listeningTextView.remove()
    }

    private fun startListening() {
        binding.lottieAnimationView.show()
        binding.listeningTextView.show()

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                binding.listeningTextView.text = getString(R.string.go_ahead_i_m_listening)
            }

            override fun onBeginningOfSpeech() {
                binding.listeningTextView.text = getString(R.string.processing_your_speech)
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                binding.listeningTextView.text = getString(R.string.got_it)
                hideListeningOverlay()
            }

            override fun onError(error: Int) {
                hideListeningOverlay()
                val message = when (error) {
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue, please try again."
                    SpeechRecognizer.ERROR_AUDIO -> "There is some problem with the microphone"
                    SpeechRecognizer.ERROR_CLIENT -> "Something went wrong, try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission needed."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that, try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
                    else -> "Speech error: $error"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                hideListeningOverlay()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    binding.messageEditText.setText(spokenText)
                    binding.messageEditText.setSelection(spokenText.length)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechIntent)
    }

    private fun setupSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
            binding.speakButton.isEnabled = false
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
        }

        binding.speakButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                    startListening()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                    showToast(requireContext(), "Microphone permission is needed for speech-to-text")
                    requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                else -> {
                    showToast(requireContext(), "Enable microphone permission from settings")
                    startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", requireContext().packageName, null)
                        }
                    )
                }
            }
        }

        binding.lottieAnimationView.setOnClickListener {
            speechRecognizer.stopListening()
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
        speechRecognizer.destroy()
        _binding = null
    }
}