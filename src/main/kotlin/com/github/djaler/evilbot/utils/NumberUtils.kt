package com.github.djaler.evilbot.utils

import java.math.BigDecimal
import java.math.MathContext

fun Long.getForm(firstForm: String, secondForm: String, thirdForm: String): String {
    return when {
        this % 100 in 11..14 -> thirdForm
        this % 10 == 1L -> firstForm
        this % 10 in 2..4 -> secondForm
        else -> thirdForm
    }
}

fun Int.getForm(firstForm: String, secondForm: String, thirdForm: String) =
    toLong().getForm(firstForm, secondForm, thirdForm)

fun BigDecimal.roundToSignificantDigitsAfterComma(mc: MathContext): BigDecimal {
    val fractionalPart = this.remainder(BigDecimal.ONE)
    return this.subtract(fractionalPart).add(fractionalPart.round(mc))
}