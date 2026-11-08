package com.example.ui.screens.accounts

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.BalanceType
import com.example.domain.util.CurrencyFormatter
import com.example.ui.components.AmountInputField
import com.example.ui.components.AppDropdownSelector
import com.example.ui.components.AppInputField
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.AppBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NegativeRed
import com.example.ui.theme.NegativeRedLight
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PositiveGreen
import com.example.ui.theme.PositiveGreenLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.AccountsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    viewModel: AccountsViewModel,
    accountId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencies by viewModel.currencies.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عملاء") }
    var amountText by remember { mutableStateOf("") }
    var balanceType by remember { mutableStateOf(BalanceType.FOR_ME) }
    var selectedCurrency by remember { mutableStateOf("SAR") }
    var notes by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(accountId, accounts) {
        if (accountId != null && accountId > 0L) {
            val existing = accounts.find { it.id == accountId }
            if (existing != null) {
                name = existing.name
                phone = existing.phone
                category = existing.category
                amountText = CurrencyFormatter.formatLiveInput(existing.openingBalance.toPlainString())
                balanceType = existing.openingBalanceType
                selectedCurrency = existing.currencyCode
                notes = existing.notes
            }
        }
    }

    if (showDeleteDialog && accountId != null) {
        ConfirmationDialog(
            title = "حذف الحساب",
            message = "هل أنت متأكد من حذف هذا الحساب؟ إذا كان يحتوي على حركات مالية مسجلة، ستتم مطالبتك بتأكيد الحذف النهائي لحماية السجلات المحاسبية.",
            confirmText = "تأكيد الحذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteAccount(accountId, force = true) { success ->
                    if (success) {
                        Toast.makeText(context, "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
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
                        text = if (accountId != null && accountId > 0L) "تعديل حساب" else "إضافة حساب جديد",
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
                    if (accountId != null && accountId > 0L) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الحساب",
                                tint = NegativeRed
                            )
                        }
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
            // 1. Account Name (Required)
            AppInputField(
                value = name,
                onValueChange = { name = it },
                label = "اسم الحساب *",
                placeholder = "مثال: شركة النور أو أحمد العلي",
                leadingIcon = Icons.Default.Business,
                testTag = "input_account_name"
            )

            // 2. Phone Number (Optional)
            AppInputField(
                value = phone,
                onValueChange = { phone = it },
                label = "رقم الهاتف",
                placeholder = "مثال: 0555123456",
                leadingIcon = Icons.Default.Phone,
                isNumeric = true,
                testTag = "input_account_phone"
            )

            // 3. Category Selector
            AppDropdownSelector(
                label = "التصنيف",
                items = listOf("عملاء", "موردون", "موظفون", "أخرى"),
                selectedItem = category,
                itemLabel = { it },
                onItemSelected = { category = it },
                testTag = "select_account_category"
            )

            // 4. Currency Selector
            AppDropdownSelector(
                label = "العملة",
                items = if (currencies.isEmpty()) listOf("SAR", "YER", "USD") else currencies.map { it.code },
                selectedItem = selectedCurrency,
                itemLabel = { code ->
                    val curr = currencies.find { it.code == code }
                    if (curr != null) "${curr.name} (${curr.symbol})" else code
                },
                onItemSelected = { selectedCurrency = it },
                testTag = "select_account_currency"
            )

            // 5. Opening Balance Amount
            AmountInputField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "الرصيد الافتتاحي",
                currencySymbol = selectedCurrency,
                testTag = "input_opening_balance"
            )

            // 6. Balance Type: لك / عليك
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "نوع الرصيد الافتتاحي *",
                    color = DeepNavy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // "لك" (Owed to user)
                    val isForMe = balanceType == BalanceType.FOR_ME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isForMe) PositiveGreenLight else Color.White)
                            .clickable { balanceType = BalanceType.FOR_ME }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لك (مستحق لنا)",
                            color = if (isForMe) PositiveGreen else NeutralTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (isForMe) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // "عليك" (Owed by user)
                    val isOnMe = balanceType == BalanceType.ON_ME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isOnMe) NegativeRedLight else Color.White)
                            .clickable { balanceType = BalanceType.ON_ME }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "عليك (مستحق علينا)",
                            color = if (isOnMe) NegativeRed else NeutralTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (isOnMe) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // 7. Notes
            AppInputField(
                value = notes,
                onValueChange = { notes = it },
                label = "ملاحظات",
                placeholder = "أي تفاصيل أو ملاحظات إضافية...",
                singleLine = false,
                testTag = "input_account_notes"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Save & Cancel
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
                        if (name.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة اسم الحساب", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.saveAccount(
                            id = accountId ?: 0L,
                            name = name,
                            phone = phone,
                            category = category,
                            amountText = amountText,
                            balanceType = balanceType,
                            currencyCode = selectedCurrency,
                            notes = notes
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "تم حفظ الحساب بنجاح", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_account_button")
                ) {
                    Text(
                        text = "حفظ الحساب",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
