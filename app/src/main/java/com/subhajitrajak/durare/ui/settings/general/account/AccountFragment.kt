package com.subhajitrajak.durare.ui.settings.general.account

import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.subhajitrajak.durare.R
import androidx.core.content.ContextCompat.getSystemService
import com.subhajitrajak.durare.auth.UserData
import com.subhajitrajak.durare.data.repositories.AccountRepository
import com.subhajitrajak.durare.databinding.FragmentAccountBinding
import com.subhajitrajak.durare.utils.showToast
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(AccountRepository(requireContext()))
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

            editWeightButton.setOnClickListener {
                userWeight.apply {
                    if (isEnabled) {
                        isEnabled = false
                        editWeightButton.setImageResource(R.drawable.edit_pencil)
                        viewModel.updateWeight(text.toString().toDouble())
                    } else {
                        isEnabled = true
                        editWeightButton.setImageResource(R.drawable.check)
                        requestFocus()
                        setSelection(text.length)
                        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
                        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
            }
        }

        viewModel.fetchUserData()

        lifecycleScope.launch {
            viewModel.userDataState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UserDataState.Loading -> {}
                    is UserDataState.Success -> showUserData(state.data)
                    is UserDataState.Error -> {
                        showToast(requireContext(), state.message)
                    }
                }
            }
        }
    }

    private fun showUserData(userData: UserData?) {
        binding.userName.text = userData?.username ?: "N/A"
        binding.userEmail.text = userData?.userEmail ?: "N/A"
        binding.userWeight.setText(userData?.userWeight?.toString() ?: "")

        Glide.with(requireContext())
            .load(userData?.profilePictureUrl)
            .into(binding.userImage)
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