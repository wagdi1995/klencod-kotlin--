package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthlyStat
import com.example.domain.util.CurrencyFormatter
import com.example.ui.theme.CardBg
import com.example.ui.theme.ChartIncomingBlue
import com.example.ui.theme.ChartOutgoingRed
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightGrayDivider
import com.example.ui.theme.NeutralTextSecondary
import java.math.BigDecimal

@Composable
fun MonthlyActivityChart(
    stats: List<MonthlyStat>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Find maximum amount for scale
    val maxVal = remember(stats) {
        val maxIn = stats.maxOfOrNull { it.totalIncoming.toFloat() } ?: 1000f
        val maxOut = stats.maxOfOrNull { it.totalOutgoing.toFloat() } ?: 1000f
        val calculated = maxOf(maxIn, maxOut, 30000f)
        // Round to nearest 10k
        val ceiling = ((calculated / 10000f).toInt() + 1) * 10000f
        maxOf(ceiling, 30000f)
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0A142D52),
                spotColor = Color(0x0A142D52)
            ),
        shape = RoundedCornerShape(24.dp),
        color = CardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("monthly_activity_chart")
        ) {
            // Header Row: Title & Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الحركة الشهرية",
                    color = DeepNavy,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Legends
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ChartIncomingBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "وارد",
                            color = NeutralTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ChartOutgoingRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "صادر",
                            color = NeutralTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Selected details tooltip
            if (selectedIndex != null && selectedIndex in stats.indices) {
                val stat = stats[selectedIndex!!]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "${stat.monthLabel}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "وارد: ${CurrencyFormatter.format(stat.totalIncoming)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartIncomingBlue
                        )
                        Text(
                            text = "صادر: ${CurrencyFormatter.format(stat.totalOutgoing)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartOutgoingRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Body: Y-Axis lines + Paired Bars
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Background grid lines (0, 10K, 20K, 30K)
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val steps = listOf("30K", "20K", "10K", "0")
                    steps.forEachIndexed { idx, label ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = NeutralTextSecondary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                modifier = Modifier.width(28.dp)
                            )
                            HorizontalDivider(
                                color = LightGrayDivider,
                                thickness = 1.dp
                            )
                        }
                    }
                }

                // Paired Bars Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 36.dp, end = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.forEachIndexed { index, stat ->
                        val isSelected = selectedIndex == index

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    selectedIndex = if (selectedIndex == index) null else index
                                }
                        ) {
                            // Paired Bars
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.height(125.dp)
                            ) {
                                // Incoming Blue Bar
                                val inFrac = (stat.totalIncoming.toFloat() / maxVal).coerceIn(0.04f, 1f) * animationProgress.value
                                Box(
                                    modifier = Modifier
                                        .width(13.dp)
                                        .fillMaxHeight(inFrac)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (isSelected) ChartIncomingBlue else ChartIncomingBlue.copy(alpha = 0.9f))
                                )

                                // Outgoing Red Bar
                                val outFrac = (stat.totalOutgoing.toFloat() / maxVal).coerceIn(0.04f, 1f) * animationProgress.value
                                Box(
                                    modifier = Modifier
                                        .width(13.dp)
                                        .fillMaxHeight(outFrac)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (isSelected) ChartOutgoingRed else ChartOutgoingRed.copy(alpha = 0.9f))
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Month Label
                            Text(
                                text = stat.monthLabel,
                                color = if (isSelected) DeepNavy else NeutralTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
