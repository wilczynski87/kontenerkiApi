package com.kontenery.data.invoice

import com.kontenery.data.Contract
import com.kontenery.data.Product
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable
data class Position(
    val productName:String? = null,
    val unitPrice:String? = null,
    val quantity:String? = null,
    val price:String? = null,
    val vatRate:String? = "23",
    val vatAmount:String? = null,
    val priceWithVat:String? = null,
) {
    companion object {
        fun toPosition(contract: Contract): Position {
            fun vatCalculate(): BigDecimal {
                val netPrice: BigDecimal = contract.netPrice?.setScale(2, RoundingMode.HALF_UP) ?: return BigDecimal.ZERO
                val vatRate: BigDecimal = contract.vatRate.setScale(2, RoundingMode.HALF_UP)
                val vatProcent = vatRate.divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                return netPrice.multiply(vatProcent).setScale(2, RoundingMode.HALF_UP)
            }

            fun getQuantity():BigDecimal {
                return if(contract.product is Product.Yard) {
                    (contract.product as Product.Yard).quantity?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP)
                        ?: BigDecimal(1)
                } else BigDecimal(1)
            }

            fun unitPriceCalculate(): BigDecimal? {
                return if(contract.product is Product.Yard) {
                    contract.netPrice?.divide(getQuantity(), 2, RoundingMode.HALF_UP)
                } else contract.netPrice?.setScale(2, RoundingMode.HALF_UP)
            }

            return Position(
                productName = contract.product?.name ?: "Błąd w nazwie",
                unitPrice = (unitPriceCalculate() ?: "Błąd w cenie").toString() ,
                quantity = getQuantity().toString(),
                price = contract.netPrice.toString(),
                vatRate = contract.vatRate.toString(),
                vatAmount = vatCalculate().toString(),
                priceWithVat = (contract.netPrice?.plus(vatCalculate()) ?: "Błąd w obliczeniach").toString()
            )
        }
    }
}
