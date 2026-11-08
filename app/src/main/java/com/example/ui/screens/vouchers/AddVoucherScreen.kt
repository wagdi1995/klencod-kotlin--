package com.example.ui.screens.vouchers

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
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
import com.example.domain.model.VoucherType
import com.example.domain.util.DateFormatter
import com.example.ui.components.AmountInputField
import com.example.ui.components.AppDropdownSelector
import com.example.ui.components.AppInputField
import com.example.ui.theme.AppBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.VouchersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVoucherScreen(
    viewModel: VouchersViewModel,
    initialType: String = "RECEIPT",
    initialAccountId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencies by viewModel.currencies.collectAsStateWithLifecycle()

    var voucherType by remember { mutableStateOf(VoucherType.fromString(initialType)) }
    var voucherNumber by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(initialAccountId ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("SAR") }
    var personName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val voucherDate by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(voucherType) {
        viewModel.getNextVoucherNumber(voucherType) { num ->
            voucherNumber = num
        }
    }

    LaunchedEffect(accounts) {
        if (selectedAccountId == 0L && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
            selectedCurrency = accounts.first().currencyCode
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
                        text = if (voucherType == VoucherType.RECEIPT) "إنشاء سند قبض" else "إنشاء سند صرف",
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
            // Voucher Type Toggle
            AppDropdownSelector(
                label = "نوع السند",
                items = listOf(VoucherType.RECEIPT, VoucherType.PAYMENT),
                selectedItem = voucherType,
                itemLabel = { it.labelArabic },
                onItemSelected = { voucherType = it },
                testTag = "select_voucher_type"
            )

            // Voucher Number
            AppInputField(
                value = voucherNumber,
                onValueChange = { voucherNumber = it },
                label = "رقم السند *",
                leadingIcon = Icons.Default.Tag,
                testTag = "input_voucher_number"
            )

            // Account Selector
            val currentAccount = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()
            if (currentAccount != null) {
                AppDropdownSelector(
                    label = "الحساب المستفيد *",
                    items = accounts,
                    selectedItem = currentAccount,
                    itemLabel = { "${it.name} (${it.category})" },
                    onItemSelected = {
                        selectedAccountId = it.id
                        selectedCurrency = it.currencyCode
                    },
                    testTag = "select_voucher_account"
                )
            }

            // Person Name (استلمنا من / صرفنا إلى)
            AppInputField(
                value = personName,
                onValueChange = { personName = it },
                label = if (voucherType == VoucherType.RECEIPT) "استلمنا من السيد/الشركة *" else "صرفنا إلى السيد/الشركة *",
                placeholder = "الاسم كاملاً",
                leadingIcon = Icons.Default.Person,
                testTag = "input_voucher_person"
            )

            // Amount Field
            AmountInputField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "المبلغ *",
                currencySymbol = selectedCurrency,
                testTag = "input_voucher_amount"
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
                testTag = "select_voucher_currency"
            )

            // Description Field
            AppInputField(
                value = description,
                onValueChange = { description = it },
                label = "وذلك عن (البيان) *",
                placeholder = "مثال: دفعة فاتورة مبيعات نقدية",
                leadingIcon = Icons.Default.Description,
                testTag = "input_voucher_desc"
            )

            // Date Field
            AppInputField(
                value = DateFormatter.formatShortDate(voucherDate),
                onValueChange = { /* Timestamp */ },
                label = "التاريخ",
                leadingIcon = Icons.Default.CalendarToday,
                testTag = "input_voucher_date"
            )

            // Notes Field
            AppInputField(
                value = notes,
                onValueChange = { notes = it },
                label = "ملاحظات إضافية",
                placeholder = "أي شروط أو بنود إضافية...",
                singleLine = false,
                testTag = "input_voucher_notes"
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
                        if (personName.isBlank() || amountText.isBlank()) {
                            Toast.makeText(context, "يرجى ملء جميع الحقول الإلزامية", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.saveVoucher(
                            voucherNumber = voucherNumber,
                            type = voucherType,
                            accountId = selectedAccountId,
                            amountText = amountText,
                            currencyCode = selectedCurrency,
                            date = voucherDate,
                            personName = personName,
                            description = description,
                            notes = notes
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "تم حفظ السند وترحيل العملية بنجاح", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } else {
                                Toast.makeText(context, "فشل حفظ السند، يرجى مراجعة البيانات", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_voucher_button")
                ) {
                    Text(
                        text = "حفظ وإصدار السند",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
