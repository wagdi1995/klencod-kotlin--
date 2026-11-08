package com.example.ui.screens.accounts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.util.CurrencyFormatter
import com.example.domain.util.PdfReportGenerator
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.TransactionItemCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.AppBg
import com.example.ui.theme.CardBg
import com.example.ui.theme.ChartIncomingBlue
import com.example.ui.theme.ChartOutgoingRed
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightBlueHeader
import com.example.ui.theme.NegativeRed
import com.example.ui.theme.NegativeRedLight
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PositiveGreen
import com.example.ui.theme.PositiveGreenLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.AccountDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val account by viewModel.account.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val totalIn by viewModel.totalIncoming.collectAsStateWithLifecycle()
    val totalOut by viewModel.totalOutgoing.collectAsStateWithLifecycle()

    val periodFilter by viewModel.periodFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()

    var showPeriodMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }

    var transactionToDelete by remember { mutableStateOf<Long?>(null) }

    if (transactionToDelete != null) {
        ConfirmationDialog(
            title = "حذف العملية المالية",
            message = "هل أنت متأكد من رغبتك في حذف هذه العملية؟ سيتم تحديث الرصيد تلقائياً.",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTransaction(transactionToDelete!!)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppBg)
            .statusBarsPadding(),
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "كشف حساب",
                        color = DeepNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = DeepNavy
                        )
                    }
                },
                actions = {
                    // Export & Share PDF button
                    IconButton(
                        onClick = {
                            val file = viewModel.exportPdf(context)
                            if (file != null) {
                                PdfReportGenerator.sharePdf(context, file, "كشف حساب ${account?.name ?: ""}")
                            } else {
                                Toast.makeText(context, "تعذر إنشاء ملف PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة كشف الحساب",
                            tint = PrimaryBlue
                        )
                    }

                    // Edit Account
                    IconButton(
                        onClick = {
                            account?.let {
                                onNavigate(Screen.AddEditAccount.createRoute(it.id))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الحساب",
                            tint = DeepNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    account?.let {
                        onNavigate(Screen.AddEditTransaction.createRoute(accountId = it.id))
                    }
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("fab_add_account_transaction")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة عملية",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        if (account == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "جاري تحميل تفاصيل الحساب...", color = NeutralTextSecondary)
            }
        } else {
            val acct = account!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Account Header Large Gradient Card (Matches Screenshot 1)
                item {
                    val headerGradient = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F0FE),
                            Color(0xFFF7FAFF),
                            Color.White
                        )
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(26.dp),
                                ambientColor = Color(0x12142D52),
                                spotColor = Color(0x12142D52)
                            ),
                        shape = RoundedCornerShape(26.dp),
                        color = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerGradient)
                                .padding(20.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Account avatar & Name & Category
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = NeutralTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = acct.name,
                                            color = DeepNavy,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = acct.category,
                                            color = NeutralTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (acct.phone.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = acct.phone,
                                                color = NeutralTextSecondary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFDCE8FD)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Business,
                                            contentDescription = acct.name,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Current Balance Label & Big Amount
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "الرصيد الحالي",
                                        color = NeutralTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = CurrencyFormatter.format(acct.currentBalance.abs()),
                                            color = if (acct.isNegative) NegativeRed else DeepNavy,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = acct.currencyCode,
                                            color = DeepNavy,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Four Compact Summary Metric Cards (Matches Screenshot 1)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Opening Balance
                        MetricSummaryCard(
                            title = "رصيد افتتاحي",
                            value = CurrencyFormatter.formatCompact(acct.openingBalance),
                            icon = Icons.Default.Wallet,
                            iconColor = PositiveGreen,
                            bgColor = PositiveGreenLight,
                            modifier = Modifier.weight(1f)
                        )

                        // 2. Total Incoming (وارد)
                        MetricSummaryCard(
                            title = "وارد",
                            value = CurrencyFormatter.formatCompact(totalIn),
                            icon = Icons.Default.ArrowDownward,
                            iconColor = ChartIncomingBlue,
                            bgColor = Color(0xFFE0F2FE),
                            modifier = Modifier.weight(1f)
                        )

                        // 3. Total Outgoing (صادر)
                        MetricSummaryCard(
                            title = "صادر",
                            value = CurrencyFormatter.formatCompact(totalOut),
                            icon = Icons.Default.ArrowUpward,
                            iconColor = ChartOutgoingRed,
                            bgColor = Color(0xFFFEECEB),
                            modifier = Modifier.weight(1f)
                        )

                        // 4. Currency / Net
                        MetricSummaryCard(
                            title = "صافي الحركة",
                            value = CurrencyFormatter.formatCompact(totalIn.subtract(totalOut)),
                            icon = Icons.Default.AttachMoney,
                            iconColor = PrimaryBlue,
                            bgColor = Color(0xFFEAF2FF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Filter Controls Row (Matches Screenshot 1: الفترة, النوع, العملة)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Period Filter
                        FilterCapsule(
                            title = "الفترة",
                            value = periodFilter,
                            expanded = showPeriodMenu,
                            onToggle = { showPeriodMenu = !showPeriodMenu },
                            onDismiss = { showPeriodMenu = false },
                            options = listOf("الكل", "هذا الأسبوع", "هذا الشهر", "آخر 3 أشهر", "هذا العام"),
                            onSelect = {
                                viewModel.periodFilter.value = it
                                showPeriodMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // 2. Type Filter
                        FilterCapsule(
                            title = "النوع",
                            value = typeFilter,
                            expanded = showTypeMenu,
                            onToggle = { showTypeMenu = !showTypeMenu },
                            onDismiss = { showTypeMenu = false },
                            options = listOf("الكل", "وارد", "صادر", "سندات", "تحويلات"),
                            onSelect = {
                                viewModel.typeFilter.value = it
                                showTypeMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // 3. Currency Filter
                        FilterCapsule(
                            title = "العملة",
                            value = acct.currencyCode,
                            expanded = showCurrencyMenu,
                            onToggle = { showCurrencyMenu = !showCurrencyMenu },
                            onDismiss = { showCurrencyMenu = false },
                            options = listOf(acct.currencyCode, "الكل"),
                            onSelect = { showCurrencyMenu = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Section Header: "الحركات"
                item {
                    Text(
                        text = "الحركات",
                        color = DeepNavy,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 5. Transactions List
                items(transactions) { trans ->
                    TransactionItemCard(
                        transaction = trans,
                        showRunningBalance = true,
                        onClick = {
                            transactionToDelete = trans.id
                        }
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد حركات مسجلة لهذا الحساب",
                                color = NeutralTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x0A000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = NeutralTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = DeepNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterCapsule(
    title: String,
    value: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onToggle),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFEDF2F9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = DeepNavy,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (value == "الكل") title else value,
                    color = DeepNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .background(CardBg)
                .clip(RoundedCornerShape(14.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = DeepNavy,
                            fontSize = 13.sp,
                            fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}
