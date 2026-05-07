package com.kontenery.service.impl

import com.kontenery.data.Reading
import com.kontenery.data.Submeter
import com.kontenery.data.invoice.Position
import com.kontenery.data.utils.UtilityType
import com.kontenery.repository.UtilitiesRepo
import com.kontenery.service.UtilitiesService
import java.math.BigDecimal
import java.math.RoundingMode

class UtilitiesServiceImpl(
    private val utilitiesRepo: UtilitiesRepo,
): UtilitiesService {
    override suspend fun getSubmeters(): List<Submeter> {
        return utilitiesRepo.getAllSubmeters()
    }

    override suspend fun getSubmeter(id: Long): Submeter? {
        return utilitiesRepo.getSubmeter(id)
    }

    override suspend fun postSubmeter(submeter: Submeter): Submeter {
        return utilitiesRepo.createSubmeter(submeter)
    }

    override suspend fun updateSubmeter(
        id: Long,
        submeter: Submeter
    ): Submeter? {
        return utilitiesRepo.updateSubmeter(id, submeter)
    }

    override suspend fun deleteSubmeter(id: Long): Boolean {
        return utilitiesRepo.deleteSubmeter(id)
    }


    // READINGS
    override suspend fun getReadings(submeterId: Long): List<Reading> {
        return utilitiesRepo.getReadingsForSubmeter(submeterId)
    }

    override suspend fun getReading(id: Long): Reading? {
        return utilitiesRepo.getReading(id)
    }

    override suspend fun postReading(reading: Reading): Reading {
        return utilitiesRepo.createReading(reading)
    }

    override suspend fun addReading(reading: Reading): Submeter? {
        val reading = postReading(reading)
        return utilitiesRepo.getSubmeter(reading.submeterId ?: throw NullPointerException("No submeterId in saved reading"))
    }

    override suspend fun updateReading(
        id: Long,
        reading: Reading
    ): Reading? {
        return utilitiesRepo.updateReading(id, reading)
    }

    override suspend fun deleteReading(id: Long): Boolean {
        return utilitiesRepo.deleteReading(id)
    }

    override suspend fun createPosition(newReading: Reading): Position {
        return try {
            if(newReading.submeterId == null) throw NullPointerException("No submeter Id $newReading")

            val submeter: Submeter = utilitiesRepo.getSubmeter(newReading.submeterId) ?: throw NullPointerException("No submeter found $newReading")
            val lastReading: Reading? = submeter.readings
                .filter { it.date != null }
                .maxByOrNull { it.date!! }

            val quantity: String = calculateReading(newReading.reading,lastReading?.reading)

            val unitPrice = newReading.currentUnitPriceNet?.toDouble().toString()
            val vatRate = if(newReading.utilityType == UtilityType.ELECTRICITY) "23" else "8"

            val price = calculatePrice(quantity, unitPrice)
            val vatAmount: String = calculateVatAmount(price, vatRate)
            val priceWithVat: String = priceWithVat(price, vatAmount)
            println("position: ${Position(
                newReading.utilityType?.name,
                unitPrice,
                quantity,
                price,
                vatRate,
                vatAmount,
                priceWithVat
            )}")

            Position(
                newReading.utilityType?.name,
                unitPrice,
                quantity,
                price,
                vatRate,
                vatAmount,
                priceWithVat
            )
        } catch (e: Exception) {
            throw NumberFormatException("could not create position ${e.message}, reading: $newReading")
        }
    }

    private fun calculateReading(newReading: BigDecimal?, oldReading: BigDecimal?): String {
        val newReading: BigDecimal = newReading ?: throw NullPointerException("No reading value found")
        val oldReading: BigDecimal = oldReading ?: BigDecimal.ZERO
        return (newReading - oldReading).toString()
    }
    private fun calculatePrice(amount: String?, unitPrice: String?): String {
        val amount: BigDecimal = amount?.toBigDecimalOrNull() ?: throw NullPointerException("No amount found")
        val unitPrice: BigDecimal = unitPrice?.toBigDecimalOrNull() ?: throw NullPointerException("No unitPrice found")
        return (amount * unitPrice).setScale(2, RoundingMode.HALF_DOWN).toString()
    }
    private fun calculateVatAmount(price: String?, vatRate: String?): String {
        val price: BigDecimal = price?.toBigDecimalOrNull() ?: throw NullPointerException("No price value found")
        val vatRate: BigDecimal = vatRate?.toBigDecimalOrNull() ?: throw NullPointerException("No vat rate")
        return ((price * vatRate) / BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_DOWN).toString()
    }

    private fun priceWithVat(price: String?, vatAmount: String?): String {
        val price: BigDecimal = price?.toBigDecimalOrNull() ?: throw NullPointerException("No price value found")
        val vatAmount: BigDecimal = vatAmount?.toBigDecimalOrNull() ?: throw NullPointerException("No vatAmount")
        return (price + vatAmount).setScale(2, RoundingMode.HALF_DOWN).toString()
    }

}