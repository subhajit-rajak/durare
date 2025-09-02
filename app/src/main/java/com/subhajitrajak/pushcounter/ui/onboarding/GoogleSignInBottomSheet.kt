package com.subhajitrajak.pushcounter.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.BottomSheetGoogleSigninBinding

class GoogleSignInBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetGoogleSigninBinding? = null
    private val binding get() = _binding!!

    var onGoogleSignInClick: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.CustomBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetGoogleSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.continueWithGoogleButton.setOnClickListener {
            onGoogleSignInClick?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GoogleSignInBottomSheet"
    }
}
