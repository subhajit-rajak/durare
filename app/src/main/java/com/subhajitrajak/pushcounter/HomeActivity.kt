package com.subhajitrajak.pushcounter

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.flexbox.FlexboxLayout

class HomeActivity : AppCompatActivity() {

    private lateinit var heatmapLayout: FlexboxLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        heatmapLayout = findViewById(R.id.heatmapLayout)

        generateSampleHeatmap()
    }

    private fun generateSampleHeatmap() {
        val daysInMonth = 30

        // Example streak data: random 0, 1, 2 values
        val streaks = (1..daysInMonth).map { (0..2).random() }

        for (day in 1..daysInMonth) {
            val circleView = TextView(this)

            val size = dpToPx(18)
            val params = FlexboxLayout.LayoutParams(size, size)
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            circleView.layoutParams = params

            circleView.text = "" // or use day.toString() if you want numbers
            circleView.gravity = android.view.Gravity.CENTER

            // Load base circle drawable
            val drawable = ContextCompat.getDrawable(this, R.drawable.day_circle)?.mutate()
            val color = when (streaks[day - 1]) {
                0 -> getColor(R.color.level_0)
                1 -> getColor(R.color.level_1)
                else -> getColor(R.color.level_2)
            }
            (drawable as GradientDrawable).setColor(color)

            circleView.background = drawable
            heatmapLayout.addView(circleView)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}