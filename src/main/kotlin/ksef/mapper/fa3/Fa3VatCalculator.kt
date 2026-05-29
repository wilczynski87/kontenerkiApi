package com.kontenery.ksef.mapper.fa3

import com.kontenery.data.invoice.Position
import com.kontenery.ksef.exception.KsefException
import java.math.BigDecimal
import java.math.RoundingMode

internal object Fa3VatCalculator {

    data class LineAmounts(
        val productName: String,
        val quantity: BigDecimal,
        val unitPrice: BigDecimal,
        val net: BigDecimal,
        val vatRate: String,
        val vat: BigDecimal,
    )

    data class VatBucket(
        val rate: String,
        val net: BigDecimal,
        val vat: BigDecimal,
    )

    fun lineAmounts(position: Position): LineAmounts {
        val quantity = parseDecimal(position.quantity, "quantity")
        val unitPrice = parseDecimal(position.unitPrice, "unitPrice")
        val declaredNet = parseDecimal(position.price, "price")
        val computedNet = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP)
        val net = if (declaredNet.subtract(computedNet).abs() <= BigDecimal("0.01")) {
            declaredNet.setScale(2, RoundingMode.HALF_UP)
        } else {
            computedNet
        }
        val vatRate = normalizeVatRate(position.vatRate)
        val computedVat = vatFromNet(net, vatRate)
        val declaredVat = position.vatAmount?.let { parseDecimal(it, "vatAmount") }
        val vat = declaredVat?.let { declared ->
            if (declared.subtract(computedVat).abs() <= BigDecimal("0.01")) {
                declared.setScale(2, RoundingMode.HALF_UP)
            } else {
                computedVat
            }
        } ?: computedVat

        return LineAmounts(
            productName = position.productName ?: "Pozycja",
            quantity = quantity,
            unitPrice = unitPrice,
            net = net,
            vatRate = vatRate,
            vat = vat,
        )
    }

    fun groupVatBuckets(lines: List<LineAmounts>): List<VatBucket> =
        lines.groupBy { it.vatRate }
            .map { (rate, items) ->
                VatBucket(
                    rate = rate,
                    net = items.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.net) },
                    vat = items.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.vat) },
                )
            }
            .sortedBy { rateSortOrder(it.rate) }

    fun normalizeVatRate(vatRate: String?): String {
        val raw = vatRate?.trim()?.removeSuffix("%")?.trim() ?: "23"
        val lowered = raw.lowercase()
        when (lowered) {
            "0", "0.0" -> return "0 KR"
            "zw" -> return "zw"
            "oo" -> return "oo"
            "np i", "np 1" -> return "np I"
            "np ii", "np 2" -> return "np II"
        }

        val numeric = raw.replace(",", ".").toBigDecimalOrNull()
        if (numeric != null) {
            val percent = if (numeric < BigDecimal.ONE) {
                numeric.multiply(BigDecimal(100))
            } else {
                numeric
            }
            val plain = percent.stripTrailingZeros().toPlainString()
            return when (plain) {
                "0" -> "0 KR"
                else -> plain
            }
        }

        return when (raw) {
            "0 KR", "0 WDT", "0 EX", "np I", "np II" -> raw
            else -> throw KsefException("Unsupported VAT rate for KSeF P_12: $vatRate")
        }
    }

    private fun vatFromNet(net: BigDecimal, rateKey: String): BigDecimal {
        if (rateKey !in Fa3Constants.VAT_RATES_WITH_TAX) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }
        val percent = BigDecimal(rateKey)
        return net.multiply(percent).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }

    private fun rateSortOrder(rate: String): Int = when (rate) {
        "23" -> 1
        "22" -> 2
        "8" -> 3
        "7" -> 4
        "5" -> 5
        "4" -> 6
        "3" -> 7
        "0 KR", "0 WDT", "0 EX" -> 8
        "zw", "oo", "np I", "np II" -> 9
        else -> 99
    }

    fun formatAmount(value: BigDecimal): String =
        value.setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun parseDecimal(value: String?, field: String): BigDecimal =
        value?.replace(",", ".")?.trim()?.toBigDecimalOrNull()
            ?: throw KsefException("Invalid decimal value for $field: $value")
}
