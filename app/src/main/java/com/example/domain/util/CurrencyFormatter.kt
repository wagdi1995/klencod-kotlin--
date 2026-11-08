package com.example.domain.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val amountFormat = DecimalFormat("#,##0.00", symbols)
    private val compactFormat = DecimalFormat("#,##0", symbols)

    fun format(amount: BigDecimal, currencySymbol: String? = null, showPlusSign: Boolean = false): String {
        val formatted = amountFormat.format(amount)
        val withSign = if (showPlusSign && amount > BigDecimal.ZERO) "+$formatted" else formatted
        return if (currencySymbol.isNullOrBlank()) {
            withSign
        } else {
            "$withSign $currencySymbol"
        }
    }

    fun formatCompact(amount: BigDecimal, currencySymbol: String? = null): String {
        val formatted = compactFormat.format(amount)
        return if (currencySymbol.isNullOrBlank()) {
            formatted
        } else {
            "$formatted $currencySymbol"
        }
    }

    fun parseAmount(text: String): BigDecimal {
        val clean = text.replace(",", "").trim()
        return try {
            if (clean.isBlank()) BigDecimal.ZERO else BigDecimal(clean).setScale(2, RoundingMode.HALF_UP)
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }

    fun formatLiveInput(input: String): String {
        val clean = input.filter { it.isDigit() || it == '.' }
        if (clean.isBlank()) return ""
        val parts = clean.split(".")
        val intPart = parts[0]
        val formattedInt = if (intPart.isNotEmpty()) {
            try {
                DecimalFormat("#,###", symbols).format(intPart.toLong())
            } catch (_: Exception) {
                intPart
            }
        } else ""

        return if (parts.size > 1) {
            val decPart = parts[1].take(2)
            "$formattedInt.$decPart"
        } else if (clean.endsWith(".")) {
            "$formattedInt."
        } else {
            formattedInt
        }
    }
}
