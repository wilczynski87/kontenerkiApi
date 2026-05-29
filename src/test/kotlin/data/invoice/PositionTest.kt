package com.kontenery.data.invoice

import com.kontenery.data.Contract
import com.kontenery.data.Product
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PositionTest {

    @Test
    fun `toPosition normalizes fractional vatRate and calculates VAT`() {
        val position = Position.toPosition(
            Contract(
                netPrice = BigDecimal("100.00"),
                vatRate = BigDecimal("0.23"),
                product = Product.Container(name = "Kontener"),
            ),
        )
        assertEquals("23", position.vatRate)
        assertEquals("23.00", position.vatAmount)
        assertEquals("123.00", position.priceWithVat)
    }
}
