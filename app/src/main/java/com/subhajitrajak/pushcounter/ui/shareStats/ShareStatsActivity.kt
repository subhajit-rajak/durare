package com.subhajitrajak.pushcounter.ui.shareStats

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.ActivityShareStatsBinding

class ShareStatsActivity : AppCompatActivity() {

    private val binding: ActivityShareStatsBinding by lazy {
        ActivityShareStatsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // get data from intent
        val pushUps = intent.getStringExtra("pushUps")?: "0"
        val time = intent.getStringExtra("time") ?: "0m 0s"
        val rest = intent.getStringExtra("rest") ?: "0m 0s"

        // setup viewpager
        val adapter = StatsPagerAdapter(this, pushUps, time, rest)
        binding.viewPager.adapter = adapter

        // setup dots indicator
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.goHome.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_PUSH_UPS = "pushUps"
        const val EXTRA_TIME = "time"
        const val EXTRA_REST = "rest"
    }
}