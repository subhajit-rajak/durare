package com.subhajitrajak.durare.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.data.models.DashboardStats
import com.subhajitrajak.durare.data.models.User
import com.subhajitrajak.durare.ui.theme.Black
import com.subhajitrajak.durare.ui.theme.DarkGrey85
import com.subhajitrajak.durare.ui.theme.Grey
import com.subhajitrajak.durare.ui.theme.Secondary
import com.subhajitrajak.durare.ui.theme.SecondaryLight
import com.subhajitrajak.durare.ui.theme.sfProDisplay
import com.subhajitrajak.durare.ui.theme.specialGothicExpanded
import com.subhajitrajak.durare.utils.formatToShortNumber
import com.subhajitrajak.durare.utils.formatWithCommas

@Composable
fun DashboardScreen(
    dashboardStats: DashboardStats?,
    monthlyPushupCounts: List<Int>,
    currentStreak: Pair<Int, Int>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    isDark: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxSize()
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.instruction_image_2),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append("Good Morning, ")
                    }
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append("Tobias")
                    }
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(weight = 1f, fill = true)
                    .align(Alignment.CenterVertically),
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

            // theme switch
            IconButton(onClick = onThemeToggle) {
                Icon(
                    painter = painterResource(id = if (isDark) R.drawable.moon else R.drawable.sun),
                    contentDescription = "Theme Switch",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Let's start your\nWorkout Session",
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Medium,
                fontSize = 36.sp,
                modifier = Modifier.weight(weight = 1f, fill = true)
            )

            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.streak_flame),
                    contentDescription = "Streak",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = currentStreak.first.formatToShortNumber(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = specialGothicExpanded,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Secondary)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Text + Button
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Do 3 exercise and\nfinish your goals",
                        fontFamily = sfProDisplay,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        color = Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start Button
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = SecondaryLight,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Start",
                                fontFamily = sfProDisplay,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = CircleShape,
                                color = Secondary,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_left),
                                    contentDescription = "Start",
                                    tint = SecondaryLight,
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .rotate(180f)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Trainer image - pinned to right, overflow at top
                Image(
                    painter = painterResource(id = R.drawable.coach),
                    contentDescription = "Trainer",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(170.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Daily Challenges",
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(weight = 1f, fill = true)
                    .align(Alignment.CenterVertically),
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkGrey85
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "See All",
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Grey
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val dailyChallengesScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(dailyChallengesScrollState)
        ) {
            ExerciseCard(title = "Pushups", sets = 4, reps = 15, currentSet = 2, totalSets = 4, style = ExerciseCardStyle.GREY)
            Spacer(modifier = Modifier.width(8.dp))
            ExerciseCard(title = "Squats", sets = 5, reps = 12, currentSet = 3, totalSets = 5, style = ExerciseCardStyle.ORANGE)
            Spacer(modifier = Modifier.width(8.dp))
            ExerciseCard(title = "Chinups", sets = 2, reps = 5, currentSet = 1, totalSets = 2, style = ExerciseCardStyle.PEACH)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Streak",
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(weight = 1f, fill = true)
                    .align(Alignment.CenterVertically),
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkGrey85
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "See All",
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Grey
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            CircleHeatmap(streaks = monthlyPushupCounts)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Global Count",
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(weight = 1f, fill = true)
                    .align(Alignment.CenterVertically),
                fontFamily = sfProDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkGrey85
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painterResource(id = R.drawable.global),
                contentDescription = "Theme Switch",
                tint = Grey,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Secondary)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = dashboardStats?.allUsersTotal?.formatWithCommas() ?: "-",
                    fontFamily = sfProDisplay,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 40.sp,
                    color = DarkGrey85
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}