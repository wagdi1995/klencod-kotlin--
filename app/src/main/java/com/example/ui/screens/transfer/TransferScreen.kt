package com.example.ui.screens.transfer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AccountModel
import com.example.domain.util.CurrencyFormatter
import com.example.domain.util.DateFormatter
import com.example.ui.components.AmountInputField
import com.example.ui.components.AppDropdownSelector
import com.example.ui.components.AppInputField
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.AppBg
import com.example.ui.theme.CardBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NegativeRed
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PositiveGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.TransferViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencies by viewModel.currencies.collectAsStateWithLifecycle()

    val sourceAccount by viewModel.sourceAccount.collectAsStateWithLifecycle()
    val destinationAccount by viewModel.destinationAccount.collectAsStateWithLifecycle()
    val amountInput by viewModel.amountInput.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val exchangeRateInput by viewModel.exchangeRateInput.collectAsStateWithLifecycle()
    val transferDate by viewModel.transferDate.collectAsStateWithLifecycle()
    val transferDescription by viewModel.transferDescription.collectAsStateWithLifecycle()
    val transferNotes by viewModel.transferNotes.collectAsStateWithLifecycle()

    var showSourceSelector by remember { mutableStateOf(false) }
    var showDestSelector by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    // Transfer Review & Confirmation Dialog
    if (showReviewDialog) {
        val amt = CurrencyFormatter.parseAmount(amountInput)
        val rate = exchangeRateInput.toDoubleOrNull() ?: 1.0
        val finalReceived = if (rate != 1.0) amt.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP) else amt

        ConfirmationDialog(
            title = "مراجعة وتأكيد التحويل",
            message = "تحويل ${CurrencyFormatter.format(amt, selectedCurrency)} من حساب ${sourceAccount?.name ?: ""} إلى حساب ${destinationAccount?.name ?: ""}.\nالمبلغ المحول للوجهة: ${CurrencyFormatter.format(finalReceived, destinationAccount?.currencyCode ?: selectedCurrency)}",
            confirmText = "تأكيد وتنفيذ التحويل",
            onConfirm = {
                showReviewDialog = false
                viewModel.executeTransfer(
                    onSuccess = {
                        Toast.makeText(context, "تم التحويل بنجاح", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = { showReviewDialog = false }
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
                        text = "تحويل بين الحسابات",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source & Destination Account Cards with Swap Button (Matches Screenshot 2)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Source Account Card ("تحويل من")
                    TransferAccountCard(
                        title = "تحويل من",
                        account = sourceAccount,
                        onClick = { showSourceSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. Destination Account Card ("تحويل إلى")
                    TransferAccountCard(
                        title = "تحويل إلى",
                        account = destinationAccount,
                        onClick = { showDestSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Circular Swap Button floating between cards
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(46.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F0FE))
                        .clickable { viewModel.swapAccounts() }
                        .testTag("swap_accounts_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "تبديل الحسابين",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Dropdowns for selecting accounts
            DropdownMenu(
                expanded = showSourceSelector,
                onDismissRequest = { showSourceSelector = false },
                modifier = Modifier.background(CardBg)
            ) {
                accounts.forEach { acct ->
                    DropdownMenuItem(
                        text = { Text("${acct.name} (${CurrencyFormatter.format(acct.currentBalance, acct.currencyCode)})") },
                        onClick = {
                            viewModel.sourceAccount.value = acct
                            viewModel.selectedCurrency.value = acct.currencyCode
                            showSourceSelector = false
                        }
                    )
                }
            }

            DropdownMenu(
                expanded = showDestSelector,
                onDismissRequest = { showDestSelector = false },
                modifier = Modifier.background(CardBg)
            ) {
                accounts.forEach { acct ->
                    DropdownMenuItem(
                        text = { Text("${acct.name} (${CurrencyFormatter.format(acct.currentBalance, acct.currencyCode)})") },
                        onClick = {
                            viewModel.destinationAccount.value = acct
                            showDestSelector = false
                        }
                    )
                }
            }

            // Transfer Form Fields (Matches Screenshot 2)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = CardBg,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Amount Field
                    AmountInputField(
                        value = amountInput,
                        onValueChange = { viewModel.amountInput.value = it },
                        label = "المبلغ",
                        currencySymbol = selectedCurrency,
                        testTag = "input_transfer_amount"
                    )

                    // Currency Field
                    AppDropdownSelector(
                        label = "العملة",
                        items = if (currencies.isEmpty()) listOf("SAR", "YER", "USD") else currencies.map { it.code },
                        selectedItem = selectedCurrency,
                        itemLabel = { code ->
                            val curr = currencies.find { it.code == code }
                            if (curr != null) "${curr.name} (${curr.symbol})" else code
                        },
                        onItemSelected = { viewModel.selectedCurrency.value = it },
                        testTag = "select_transfer_currency"
                    )

                    // Exchange rate if source and dest currencies differ
                    if (sourceAccount != null && destinationAccount != null && sourceAccount!!.currencyCode != destinationAccount!!.currencyCode) {
                        AppInputField(
                            value = exchangeRateInput,
                            onValueChange = { viewModel.exchangeRateInput.value = it },
                            label = "سعر الصرف (${sourceAccount!!.currencyCode} إلى ${destinationAccount!!.currencyCode})",
                            placeholder = "1.0",
                            isNumeric = true,
                            testTag = "input_exchange_rate"
                        )
                    }

                    // Date Field
                    AppInputField(
                        value = DateFormatter.formatShortDate(transferDate),
                        onValueChange = { /* Auto-populated */ },
                        label = "التاريخ",
                        leadingIcon = Icons.Default.CalendarToday,
                        testTag = "input_transfer_date"
                    )

                    // Description Field
                    AppInputField(
                        value = transferDescription,
                        onValueChange = { viewModel.transferDescription.value = it },
                        label = "وصف التحويل",
                        placeholder = "مثال: تسوية دفعة فاتورة",
                        leadingIcon = Icons.Default.Description,
                        testTag = "input_transfer_description"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Blue Button: "مراجعة وتأكيد التحويل"
            Button(
                onClick = {
                    val amt = CurrencyFormatter.parseAmount(amountInput)
                    if (sourceAccount == null || destinationAccount == null) {
                        Toast.makeText(context, "يرجى تحديد الحسابات", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (sourceAccount!!.id == destinationAccount!!.id) {
                        Toast.makeText(context, "لا يمكن التحويل لنفس الحساب", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (amt <= BigDecimal.ZERO) {
                        Toast.makeText(context, "يرجى إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showReviewDialog = true
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_transfer_button")
            ) {
                Text(
                    text = "مراجعة وتأكيد التحويل",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TransferAccountCard(
    title: String,
    account: AccountModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = NeutralTextSecondary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (account != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = title,
                        color = NeutralTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = account.name,
                        color = DeepNavy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.format(account.currentBalance, account.currencyCode),
                        color = if (account.isNegative) NegativeRed else DeepNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "حساب جاري •• ${account.accountNumber.ifBlank { "0000" }}",
                        color = NeutralTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (title.contains("من")) Color(0xFFEAF2FF) else Color(0xFFE6F7F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (title.contains("من")) Icons.Default.Business else Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (title.contains("من")) PrimaryBlue else PositiveGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Text(
                    text = "$title (اضغط للاختيار)",
                    color = NeutralTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
