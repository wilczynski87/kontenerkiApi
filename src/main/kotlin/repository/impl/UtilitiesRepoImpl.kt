package com.kontenery.repository.impl

import com.kontenery.library.model.Reading
import com.kontenery.library.model.Submeter
import com.kontenery.library.utils.now
import com.kontenery.repository.UtilitiesRepo
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.ReadingEntity
import com.kontenery.repository.entity.ReadingTable
import com.kontenery.repository.entity.SubmeterEntity
import com.kontenery.repository.entity.SubmeterTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UtilitiesRepoImpl: UtilitiesRepo {
    // ---------- SUBMETER ----------
    override suspend fun createSubmeter(submeter: Submeter): Submeter =
        newSuspendedTransaction {
            SubmeterEntity.new {
                client = submeter.clientId?.let { ClientEntity.findById(it) }
                location = submeter.location ?: throw NullPointerException("Nie ma lokalizacji dla podlicznika")
                number = submeter.number
                utilityType = submeter.utilityType?.name ?: throw NullPointerException("Nie ma typu dla podlicznika")
            }.toDomain()
        }

    override suspend fun getSubmeter(id: Long): Submeter? =
        newSuspendedTransaction {
            SubmeterEntity.findById(id)?.toDomain()
        }

    override suspend fun getAllSubmeters(): List<Submeter> =
        newSuspendedTransaction {
            SubmeterEntity.all().map { it.toDomain() }
        }

    override suspend fun getAllSubmetersForClient(clientId: Long): List<Submeter> =
        newSuspendedTransaction {
            SubmeterEntity.find { SubmeterTable.client eq clientId }
                .map { it.toDomain() }
        }

    override suspend fun updateSubmeter(submeterId: Long, submeter: Submeter): Submeter? =
        newSuspendedTransaction {
            val entity = SubmeterEntity.findById(submeterId) ?: return@newSuspendedTransaction null
            entity.apply {
                client = submeter.clientId?.let { ClientEntity.findById(it) }
                location = submeter.location ?: location
                number = submeter.number ?: number
                utilityType = submeter.utilityType?.name ?: utilityType
            }.toDomain()
        }

    override suspend fun deleteSubmeter(id: Long): Boolean =
        newSuspendedTransaction {
            val entity = SubmeterEntity.findById(id) ?: return@newSuspendedTransaction false
            entity.delete()
            true
        }

    // ---------- READING ----------
    override suspend fun createReading(reading: Reading): Reading =
        newSuspendedTransaction {
            val submeterId = reading.submeterId ?: throw NullPointerException("Nie ma podlicznika")
            ReadingEntity.new {
                submeter = SubmeterEntity[submeterId]
                utilityType = reading.utilityType?.name ?: throw NullPointerException("Nie ma typu mediów")
                this.reading = reading.reading ?: throw NullPointerException("Nie ma odczytu zużycia")
                date = reading.date ?: LocalDate.now()
                currentUnitPriceNet = reading.currentUnitPriceNet?.toDouble() ?: throw NullPointerException("Nie ma ceny za jednostkę mediów")
            }.toDomain()
        }

    override suspend fun getReading(id: Long): Reading? =
        newSuspendedTransaction {
            ReadingEntity.findById(id)?.toDomain()
        }

    override suspend fun getReadingsForSubmeter(submeterId: Long): List<Reading> =
        newSuspendedTransaction {
            ReadingEntity.find { ReadingTable.submeter eq submeterId }
                .map { it.toDomain() }
        }

    override suspend fun updateReading(readingId: Long, reading: Reading): Reading? =
        newSuspendedTransaction {
            val submeterId = reading.submeterId ?: throw NullPointerException("Nie ma podlicznika")
            val entity = ReadingEntity.findById(readingId) ?: return@newSuspendedTransaction null
            entity.apply {
                submeter = SubmeterEntity[submeterId]
                utilityType = reading.utilityType?.name ?: utilityType
                this.reading = reading.reading ?: this.reading
                date = reading.date ?: date
                currentUnitPriceNet = reading.currentUnitPriceNet?.toDouble() ?: currentUnitPriceNet
            }.toDomain()
        }

    override suspend fun deleteReading(id: Long): Boolean =
        newSuspendedTransaction {
            val entity = ReadingEntity.findById(id) ?: return@newSuspendedTransaction false
            entity.delete()
            true
        }
}