package com.kontenery.repository.impl

import com.kontenery.data.ksef.KsefSessionInvoiceStatus
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.repository.KsefSessionInvoiceStatusRepo
import com.kontenery.repository.entity.SubmeterTable.number
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusEntity
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusTable
import com.kontenery.repository.entity.ksef.applyFromResponse
import com.kontenery.repository.entity.suspendTransaction
import org.jetbrains.exposed.sql.SortOrder

class KsefSessionInvoiceStatusRepoImpl : KsefSessionInvoiceStatusRepo {

    override suspend fun save(
        invoiceNumber: String,
        status: KsefSessionInvoiceStatusResponse,
    ): KsefSessionInvoiceStatus = suspendTransaction {
        val invoiceEntity = InvoiceEntity.find {
            InvoiceTable.invoiceNumber eq number
        }.firstOrNull() ?: throw IllegalArgumentException("Invoice not found: $invoiceNumber")

        val entity = KsefSessionInvoiceStatusEntity.new {
            invoice = invoiceEntity
            applyFromResponse(status)
        }
        entity.toDomain()
    }

    override suspend fun getLatestByInvoiceId(invoiceId: Long): KsefSessionInvoiceStatus? = suspendTransaction {
        KsefSessionInvoiceStatusEntity.find {
            KsefSessionInvoiceStatusTable.invoice eq invoiceId
        }
            .orderBy(KsefSessionInvoiceStatusTable.id to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun getAllByInvoiceId(invoiceId: Long): List<KsefSessionInvoiceStatus> = suspendTransaction {
        KsefSessionInvoiceStatusEntity.find {
            KsefSessionInvoiceStatusTable.invoice eq invoiceId
        }
            .orderBy(KsefSessionInvoiceStatusTable.id to SortOrder.DESC)
            .map { it.toDomain() }
    }
}
