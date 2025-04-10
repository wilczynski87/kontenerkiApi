package com.kontenery.model

import java.time.LocalDate


open class Product(
    open val id: Long? = null,
    open val name: String? = null,
    open val location: String? = null,
)

data class Container(
    override val id: Long? = null,
    override val name: String? = null,
    override val location: String? = null,
    val length: String? = null,
    val height: String? = null,
    val color: String? = null,
    val acquireDate: LocalDate? = null,
    val lastPainting: LocalDate? = null,
    val description: String? = null,
    val photo: String? = null,
): Product(id, name, location) {
    val uom: String = "szt"
}

data class Yard(
    override val id: Long? = null,
    override val name: String? = null,
    override val location: String? = null,
    val quantity: Long? = null
): Product(id, name, location) {
    val uom: String = "m2"
}