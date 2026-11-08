package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TransactionModel
import com.example.domain.util.CurrencyFormatter
import com.example.domain.util.DateFormatter
import com.example.ui.theme.CardBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NegativeRed
import com.example.ui.theme.NegativeRedLight
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PositiveGreen
import com.example.ui.theme.PositiveGreenLight
import java.math.BigDecimal

@Composable
fun TransactionItemCard(
    transaction: TransactionModel,
    onClick: () -> Unit,
    showRunningBalance: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isIncoming = transaction.type.isIncoming
    val amountColor = if (isIncoming) PositiveGreen else NegativeRed
    val sign = if (isIncoming) "+" else "-"

    val (icon, iconBg, iconColor) = when {
        transaction.type.isTransfer -> Triple(Icons.Default.SwapHoriz, Color(0xFFE0F2FE), Color(0xFF0284C7))
        transaction.type.isVoucher -> Triple(Icons.AutoMirrored.Filled.ReceiptLong, if (isIncoming) PositiveGreenLight else NegativeRedLight, amountColor)
        isIncoming -> Triple(Icons.Default.Description, PositiveGreenLight, PositiveGreen)
        else -> Triple(Icons.Default.ShoppingCart, NegativeRedLight, NegativeRed)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x08142D52),
                spotColor = Color(0x08142D52)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Amount & Date/Time or Running Balance on the Left (in RTL layout)
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$sign${CurrencyFormatter.format(transaction.amount, transaction.currencyCode)}",
                    color = amountColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                if (showRunningBalance && transaction.runningBalance.compareTo(BigDecimal.ZERO) != 0) {
                    Text(
                        text = "الرصيد: ${CurrencyFormatter.format(transaction.runningBalance, transaction.currencyCode)}",
                        color = NeutralTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = DateFormatter.formatRelative(transaction.date),
                        color = NeutralTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Account Name & Description on the Right (in RTL layout)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = transaction.accountName.ifBlank { transaction.title },
                    color = DeepNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = transaction.description.ifBlank { transaction.title },
                    color = NeutralTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Transaction Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.type.labelArabic,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
