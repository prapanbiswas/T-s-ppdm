package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    const val TAKA_SYMBOL = "৳"
    const val TAKA_CODE = "BDT"
    const val TAKA_TEXT = "Tk"

    /**
     * Formats a monetary value in Bangladeshi Taka (BDT) with symbol.
     * Example: 250.0 -> "৳ 250", 1500.5 -> "৳ 1,500.50"
     */
    fun formatTaka(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "$TAKA_SYMBOL %,.0f", amount)
        } else {
            String.format(Locale.US, "$TAKA_SYMBOL %,.2f", amount)
        }
    }

    /**
     * PDF safe formatting (Tk 250) to avoid font glyph issues on all PDF renderers.
     */
    fun formatTakaPdf(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "Tk %,.0f", amount)
        } else {
            String.format(Locale.US, "Tk %,.2f", amount)
        }
    }
}
