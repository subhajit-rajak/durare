package com.subhajitrajak.pushcounter.ui.shareStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhajitrajak.pushcounter.databinding.FragmentVerticalStatsBinding

class VerticalStatsFragment : Fragment() {

    private var _binding: FragmentVerticalStatsBinding? = null
    private val binding get() = _binding!!

    private var pushUps: String = ""
    private var time: String = ""
    private var rest: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pushUps = it.getString("pushUps", "")
            time = it.getString("time", "")
            rest = it.getString("rest", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerticalStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            pushUpCount.text = pushUps
            totalTimeCount.text = time
            restTime.text = rest
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(pushUps: String, time: String, rest: String) =
            VerticalStatsFragment().apply {
                arguments = Bundle().apply {
                    putString("pushUps", pushUps)
                    putString("time", time)
                    putString("rest", rest)
                }
            }
    }
}