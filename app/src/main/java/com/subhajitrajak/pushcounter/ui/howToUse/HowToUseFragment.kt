package com.subhajitrajak.pushcounter.ui.howToUse

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentHowToUseBinding

class HowToUseFragment : Fragment() {
    private var _binding: FragmentHowToUseBinding? = null
    private val binding get() = _binding!!

    private val array = arrayOf(
        "https://github.com/user-attachments/assets/ccab1d52-34d5-4978-9bd9-4e3783e6a9e5",
        "https://github.com/user-attachments/assets/2dcbd196-19df-4723-8aea-ff08b10999ea",
        "https://github.com/user-attachments/assets/ea83be08-9728-4596-914a-94e28d8dcfa7",
        "https://github.com/user-attachments/assets/4dbf3329-12a6-418e-b319-178a9f525c6f"
    )

    private lateinit var adapter: HowToUseAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val slideInterval: Long = 3000 // 3 seconds

    private val slideRunnable = object : Runnable {
        override fun run() {
            val nextItem = (binding.viewPager.currentItem + 1) % adapter.itemCount
            binding.viewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(this, slideInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHowToUseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HowToUseAdapter(requireContext(), array)
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(slideRunnable, slideInterval)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(slideRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}