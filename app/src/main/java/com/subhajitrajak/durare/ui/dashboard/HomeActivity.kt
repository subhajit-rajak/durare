package com.subhajitrajak.durare.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.databinding.ActivityHomeBinding
import com.subhajitrajak.durare.ui.counter.CounterActivity
import com.subhajitrajak.durare.utils.Preferences

class HomeActivity : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
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
            val shouldShow = (destination.id == R.id.dashboardFragment) || (destination.id == R.id.leaderboardFragment) || (destination.id == R.id.analyticsFragment)
            binding.bottomNav.visibility = if (shouldShow) View.VISIBLE else View.GONE
            binding.view.visibility = if (shouldShow) View.VISIBLE else View.GONE
            binding.startButton.visibility = if (shouldShow) View.VISIBLE else View.GONE
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
                val prefs = Preferences.getInstance(this@HomeActivity)
                prefs.setTotalReps(totalReps)
                prefs.setRestTime(restTimeMs)

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
            putExtra(Preferences.KEY_TOTAL_REPS, totalReps)
            putExtra(Preferences.KEY_REST_TIME, restTimeMs)
        }
        startActivity(intent)
    }
}