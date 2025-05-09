package com.kontenery.model.invoice

data class Position(
    val productName:String,
    val unitPrice:String,
    val quantity:String,
    val vatRate:String = "23",
    val vatAmount:String,
    val price:String,
    val vat:String,
    val priceWithVat:String,
)
