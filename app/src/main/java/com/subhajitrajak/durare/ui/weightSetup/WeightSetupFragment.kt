package com.subhajitrajak.durare.ui.weightSetup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.databinding.FragmentWeightSetupBinding
import com.subhajitrajak.durare.utils.log
import java.util.Locale

class WeightSetupFragment : Fragment() {
    private var _binding: FragmentWeightSetupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightSetupViewModel by viewModels {
        WeightSetupViewModelFactory(requireContext())
    }

    var weight: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkSavedData()
        viewModel.isDataSaved.observe(viewLifecycleOwner) { isDataSaved ->
            if (isDataSaved) {
                navigateToNextScreen()
            }
        }

        binding.apply {
            nextButton.setOnClickListener {
                val input = weightInputField.text.toString().trim()
                if (input.isEmpty()) {
                    weightInputField.error = getString(R.string.enter_your_weight)
                    return@setOnClickListener
                }

                try {
                    val inputValue = input.toDouble()
                    val unit = inputTypeField.text.toString()

                    weight = if (unit == getString(R.string.pounds)) {
                        val converted = inputValue * 0.453592
                        String.format(Locale.US, "%.2f", converted).toDouble()
                    } else {
                        String.format(Locale.US, "%.2f", inputValue).toDouble()
                    }

                    viewModel.saveWeightData(weight)
                    navigateToNextScreen()
                } catch (_: NumberFormatException) {
                    weightInputField.error = getString(R.string.invalid_weight)
                }
            }

            inputTypeCard.setOnClickListener {
                val text = inputTypeField.text
                if (text == getString(R.string.kilograms)) {
                    inputTypeField.text = getString(R.string.pounds)
                } else {
                    inputTypeField.text = getString(R.string.kilograms)
                }
            }
        }
    }

    private fun navigateToNextScreen() {
        try {
            val navController = findNavController()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.weightSetupFragment, true)
                .build()

            navController.navigate(
                R.id.action_weightSetupFragment_to_cameraAccessFragment,
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