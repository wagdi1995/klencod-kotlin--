package com.example.ui.screens.transactions

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.TransactionType
import com.example.domain.util.CurrencyFormatter
import com.example.domain.util.DateFormatter
import com.example.ui.components.AmountInputField
import com.example.ui.components.AppDropdownSelector
import com.example.ui.components.AppInputField
import com.example.ui.theme.AppBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: TransactionsViewModel,
    initialAccountId: Long? = null,
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencies by viewModel.currencies.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var selectedAccountId by remember { mutableStateOf(initialAccountId ?: 0L) }
    var selectedType by remember { mutableStateOf(TransactionType.INCOMING) }
    var amountText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("SAR") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(accounts) {
        if (selectedAccountId == 0L && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
            selectedCurrency = accounts.first().currencyCode
        }
    }

    LaunchedEffect(transactionId, allTransactions) {
        if (transactionId != null && transactionId > 0L) {
            val trans = allTransactions.find { it.id == transactionId }
            if (trans != null) {
                selectedAccountId = trans.accountId
                selectedType = trans.type
                amountText = CurrencyFormatter.formatLiveInput(trans.amount.toPlainString())
                selectedCurrency = trans.currencyCode
                title = trans.title
                description = trans.description
                notes = trans.notes
                transactionDate = trans.date
            }
        }
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
                        text = if (transactionId != null && transactionId > 0L) "تعديل عملية" else "إضافة عملية مالية",
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Selector
            val currentAccount = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()
            if (currentAccount != null) {
                AppDropdownSelector(
                    label = "اختيار الحساب *",
                    items = accounts,
                    selectedItem = currentAccount,
                    itemLabel = { "${it.name} (${it.category})" },
                    onItemSelected = {
                        selectedAccountId = it.id
                        selectedCurrency = it.currencyCode
                    },
                    testTag = "select_transaction_account"
                )
            }

            // Transaction Type Selector
            val transactionTypes = listOf(
                TransactionType.INCOMING,
                TransactionType.OUTGOING,
                TransactionType.DEBIT_NOTE,
                TransactionType.CREDIT_NOTE
            )
            AppDropdownSelector(
                label = "نوع العملية *",
                items = transactionTypes,
                selectedItem = selectedType,
                itemLabel = { it.labelArabic },
                onItemSelected = { selectedType = it },
                testTag = "select_transaction_type"
            )

            // Amount Field
            AmountInputField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "المبلغ *",
                currencySymbol = selectedCurrency,
                testTag = "input_transaction_amount"
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
                onItemSelected = { selectedCurrency = it },
                testTag = "select_transaction_currency"
            )

            // Title Field
            AppInputField(
                value = title,
                onValueChange = { title = it },
                label = "البيان أو العنوان *",
                placeholder = "مثال: دفعة فاتورة مبيعات",
                leadingIcon = Icons.Default.Description,
                testTag = "input_transaction_title"
            )

            // Description Field
            AppInputField(
                value = description,
                onValueChange = { description = it },
                label = "التفاصيل",
                placeholder = "تفاصيل إضافية عن الحركة...",
                testTag = "input_transaction_desc"
            )

            // Date Field
            AppInputField(
                value = DateFormatter.formatShortDate(transactionDate),
                onValueChange = { /* Timestamp */ },
                label = "التاريخ",
                leadingIcon = Icons.Default.CalendarToday,
                testTag = "input_transaction_date"
            )

            // Notes Field
            AppInputField(
                value = notes,
                onValueChange = { notes = it },
                label = "ملاحظات",
                placeholder = "ملاحظات محاسبية...",
                singleLine = false,
                testTag = "input_transaction_notes"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        color = NeutralTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة بيان العملية", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.saveTransaction(
                            id = transactionId ?: 0L,
                            accountId = selectedAccountId,
                            type = selectedType,
                            amountText = amountText,
                            currencyCode = selectedCurrency,
                            date = transactionDate,
                            title = title,
                            description = description,
                            notes = notes
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "تم حفظ العملية بنجاح", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } else {
                                Toast.makeText(context, "يرجى التأكد من صحة البيانات", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(
                        text = "حفظ العملية",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
