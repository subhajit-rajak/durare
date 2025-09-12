package com.subhajitrajak.pushcounter.ui.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.databinding.ActivityHomeBinding
import com.subhajitrajak.pushcounter.ui.counter.CounterActivity
import com.subhajitrajak.pushcounter.utils.Constants.KEY_REST_TIME
import com.subhajitrajak.pushcounter.utils.Constants.KEY_TOTAL_REPS
import com.subhajitrajak.pushcounter.utils.Constants.PREFS_NAME

class HomeActivity : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val shouldShow = (destination.id == R.id.dashboardFragment) || (destination.id == R.id.leaderboardFragment)
            binding.bottomNav.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }

        binding.startButton.setOnClickListener {
            showWorkoutSetupDialog()
        }
    }

    // opens the workout setup dialog to set custom rep count and rest time using time pickers
    private fun showWorkoutSetupDialog() {
        val dialog = WorkoutSetupDialog(0)
        dialog.onStartClick = { totalReps, restTimeMs ->
            dialog.binding.apply {
                // set preferences
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit { putInt(KEY_TOTAL_REPS, totalReps) }
                prefs.edit { putLong(KEY_REST_TIME, restTimeMs) }

                // Start the workout with the specified parameters
                startWorkout(totalReps, restTimeMs)
                dialog.dismiss()
            }
        }
        dialog.show(supportFragmentManager, WorkoutSetupDialog.TAG)
    }

    // navigates to the counter activity with the specified parameters
    private fun startWorkout(totalReps: Int, restTimeMs: Long) {
        val intent = Intent(this, CounterActivity::class.java).apply {
            putExtra(CounterActivity.TOTAL_REPS, totalReps)
            putExtra(CounterActivity.REST_TIME, restTimeMs)
        }
        startActivity(intent)
    }
}