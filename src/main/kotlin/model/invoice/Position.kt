package com.kontenery.model.invoice

import com.kontenery.model.Contract
import com.kontenery.model.Product
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable
data class Position(
    val productName:String,
    val unitPrice:String,
    val quantity:String,
    val price:String,
    val vatRate:String = "23",
    val vatAmount:String,
    val priceWithVat:String,
) {

    companion object {
        fun toPosition(contract: Contract): Position {
            fun vatCalculate(): BigDecimal {
                val netPrice: BigDecimal = contract.netPrice ?: return BigDecimal.ZERO
                val vatRate: BigDecimal = contract.vatRate
                val vatProcent = vatRate.divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                return netPrice.multiply(vatProcent).setScale(2, RoundingMode.HALF_UP)
            }

            fun getQuantity():BigDecimal {
                return if(contract.product is Product.Yard) {
                    (contract.product as Product.Yard).quantity?.toBigDecimal() ?: BigDecimal(1)
                } else BigDecimal(1)
            }

            fun unitPriceCalculate(): BigDecimal? {
                return if(contract.product is Product.Yard) {
                    contract.netPrice?.divide(getQuantity())?.setScale(2, RoundingMode.HALF_UP)
                } else contract.netPrice?.setScale(2, RoundingMode.HALF_UP)
            }

            return Position(
                productName = contract.product?.name ?: "Błąd w nazwie",
                unitPrice = unitPriceCalculate().toString() ?: "Błąd w cenie",
                quantity = getQuantity().toString(),
                price = contract.netPrice.toString(),
                vatRate = contract.vatRate.toString(),
                vatAmount = vatCalculate().toString(),
                priceWithVat = contract.netPrice?.plus(vatCalculate()).toString() ?: "Błąd w obliczeniach"
            )
        }
    }


}
