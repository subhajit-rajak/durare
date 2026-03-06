package com.subhajitrajak.durare.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subhajitrajak.durare.ui.theme.Black
import com.subhajitrajak.durare.ui.theme.Grey
import com.subhajitrajak.durare.ui.theme.Primary
import com.subhajitrajak.durare.ui.theme.Secondary
import com.subhajitrajak.durare.ui.theme.Transparent
import com.subhajitrajak.durare.ui.theme.VeryLightGrey
import com.subhajitrajak.durare.ui.theme.White
import com.subhajitrajak.durare.ui.theme.sfProDisplay

enum class ExerciseCardStyle {
    GREY, ORANGE, PEACH
}

@Composable
fun ExerciseCard(
    title: String,
    sets: Int,
    reps: Int,
    currentSet: Int,
    totalSets: Int,
    style: ExerciseCardStyle = ExerciseCardStyle.GREY,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (style) {
        ExerciseCardStyle.GREY -> VeryLightGrey
        ExerciseCardStyle.ORANGE -> Primary
        ExerciseCardStyle.PEACH -> Secondary
    }
    val textColor = when (style) {
        ExerciseCardStyle.ORANGE -> White
        else -> Black
    }
    val subTextColor = when (style) {
        ExerciseCardStyle.ORANGE -> White
        else -> Grey
    }
    val ringTrackColor = when (style) {
        ExerciseCardStyle.GREY -> Color(0xFFFFD2B7)
        ExerciseCardStyle.ORANGE -> White.copy(alpha = 0.25f)
        ExerciseCardStyle.PEACH -> White.copy(alpha = 0.5f)
    }
    val ringProgressColor = when (style) {
        ExerciseCardStyle.ORANGE -> Color.White
        else -> Primary
    }

    Card(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$sets sets, $reps reps",
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = subTextColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = ringTrackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringProgressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * (currentSet.toFloat() / totalSets.toFloat()),
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Transparent,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentSet.toString(),
                            fontFamily = sfProDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

class ExerciseCardStyleProvider : PreviewParameterProvider<ExerciseCardStyle> {
    override val values = sequenceOf(
        ExerciseCardStyle.GREY,
        ExerciseCardStyle.ORANGE,
        ExerciseCardStyle.PEACH
    )
}

@Preview(showBackground = true)
@Composable
fun ExerciseCardPreview(
    @PreviewParameter(ExerciseCardStyleProvider::class) style: ExerciseCardStyle
) {
    ExerciseCard(
        title = "Pushups",
        sets = 4,
        reps = 15,
        currentSet = 2,
        totalSets = 4,
        style = style
    )
}