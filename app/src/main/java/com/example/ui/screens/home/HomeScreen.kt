package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.util.DateFormatter
import com.example.ui.components.FinancialSummaryCard
import com.example.ui.components.MonthlyActivityChart
import com.example.ui.components.QuickActionsRow
import com.example.ui.components.TransactionItemCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.AppBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NegativeRed
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header Row (Matches Screenshot 4)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification Bell with Red Badge on Left (in RTL layout)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { /* Notifications preview */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "التنبيهات",
                        tint = DeepNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    // Red notification badge dot
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(NegativeRed)
                            .align(Alignment.TopEnd)
                    )
                }

                // Brand Name & Logo on Right (in RTL layout)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "حساباتي",
                            color = DeepNavy,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "مستقبل مالي أفضل",
                            color = NeutralTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "حساباتي",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Welcome & Date Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateFormatter.formatFullDateArabic(System.currentTimeMillis()),
                    color = NeutralTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = "مرحباً بك",
                    color = DeepNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. Main Financial Summary Card
        item {
            FinancialSummaryCard(summary = summary)
        }

        // 4. Monthly Financial Activity Chart
        item {
            MonthlyActivityChart(stats = monthlyStats)
        }

        // 5. Quick Actions Row
        item {
            QuickActionsRow(
                onActionClick = { actionId ->
                    when (actionId) {
                        "add_account" -> onNavigate(Screen.AddEditAccount.createRoute())
                        "add_transaction" -> onNavigate(Screen.AddEditTransaction.createRoute())
                        "transfer" -> onNavigate(Screen.Transfer.createRoute())
                        "receipt_voucher" -> onNavigate(Screen.AddVoucher.createRoute("RECEIPT"))
                        "payment_voucher" -> onNavigate(Screen.AddVoucher.createRoute("PAYMENT"))
                        "reports" -> onNavigate(Screen.Reports.route)
                    }
                }
            )
        }

        // 6. Recent Operations Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "عرض الكل >" on left (in RTL)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(Screen.AllTransactions.route) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("view_all_transactions_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "عرض الكل",
                        color = PrimaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "آخر العمليات",
                    color = DeepNavy,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 7. Recent Transactions List
        items(recentTransactions) { trans ->
            TransactionItemCard(
                transaction = trans,
                onClick = { onNavigate(Screen.AccountDetail.createRoute(trans.accountId)) }
            )
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد عمليات مسجلة حتى الآن",
                        color = NeutralTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
