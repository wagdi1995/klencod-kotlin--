package com.example

import com.example.domain.model.BalanceType
import com.example.domain.model.TransactionType
import com.example.domain.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class AccountingCalculationTest {

    @Test
    fun testCurrencyFormatting() {
        val amount = BigDecimal("48250.00")
        val formatted = CurrencyFormatter.format(amount, "ر.س")
        assertEquals("48,250.00 ر.س", formatted)
    }

    @Test
    fun testLiveInputFormatting() {
        assertEquals("1,000", CurrencyFormatter.formatLiveInput("1000"))
        assertEquals("12,500.00", CurrencyFormatter.formatLiveInput("12500.00"))
        assertEquals("1,250,000", CurrencyFormatter.formatLiveInput("1250000"))
    }

    @Test
    fun testAccountSignedBalanceLogic() {
        // Test account with positive opening balance (FOR_ME "لك")
        val opening = BigDecimal("5000.00")
        val signedOpening = opening // +5000

        // Incoming movements: +9800
        val incoming = BigDecimal("9800.00")

        // Outgoing movements: -2500
        val outgoing = BigDecimal("2500.00")

        // Current = Opening + Incoming - Outgoing
        val currentBalance = signedOpening.add(incoming).subtract(outgoing)

        assertEquals(BigDecimal("12300.00"), currentBalance)
        assertTrue(currentBalance > BigDecimal.ZERO) // "لك"
    }

    @Test
    fun testNegativeLiabilityBalanceLogic() {
        // Test account with liability (ON_ME "عليك")
        val opening = BigDecimal("2600.00").negate() // -2600

        // Outgoing movement (e.g. payment to supplier): -1500
        val outgoing = BigDecimal("1500.00")

        val currentBalance = opening.subtract(outgoing)
        assertEquals(BigDecimal("-4100.00"), currentBalance)
        assertTrue(currentBalance < BigDecimal.ZERO) // "عليك"
    }

    @Test
    fun testNetConsolidatedBalance() {
        val totalForMe = BigDecimal("62400.00")
        val totalOnMe = BigDecimal("14150.00")
        val netTotal = totalForMe.subtract(totalOnMe)

        assertEquals(BigDecimal("48250.00"), netTotal)
    }
}
