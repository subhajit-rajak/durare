package com.subhajitrajak.durare.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.subhajitrajak.durare.ui.theme.Level0
import com.subhajitrajak.durare.ui.theme.Level1
import com.subhajitrajak.durare.ui.theme.Level2
import com.subhajitrajak.durare.ui.theme.Level3
import com.subhajitrajak.durare.ui.theme.Level4

@Composable
fun CircleHeatmap(
    streaks: List<Int>,
    dotSize: Dp = 16.dp,
    spacing: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    val colors = listOf(Level0, Level1, Level2, Level3, Level4)
    val maxValue = streaks.maxOrNull() ?: 0

    val levels = streaks.map { value ->
        when {
            value == 0 -> 0
            maxValue == 0 -> 0
            else -> {
                val ratio = value.toFloat() / maxValue
                (ratio * 4).toInt().coerceIn(1, 4)
            }
        }
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        levels.forEach { level ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(colors[level])
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircleHeatmapPreview() {
    val streaks = List(120) { (0..10).random() } // raw values, not levels

    Box(modifier = Modifier.padding(16.dp)) {
        CircleHeatmap(streaks = streaks)
    }
}