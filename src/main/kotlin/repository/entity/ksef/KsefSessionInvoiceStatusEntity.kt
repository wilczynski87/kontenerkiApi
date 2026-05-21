package com.kontenery.repository.entity.ksef

import com.kontenery.data.ksef.KsefSessionInvoiceStatus
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefStatusInfo
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object KsefSessionInvoiceStatusTable : LongIdTable("ksef_session_invoice_status") {
    val invoice = reference("invoice_id", InvoiceTable, onDelete = ReferenceOption.CASCADE)
    val referenceNumber = varchar("reference_number", 100).nullable()
    val invoiceNumber = varchar("invoice_number", 50).nullable()
    val ksefNumber = varchar("ksef_number", 100).nullable()
    val statusCode = integer("status_code").nullable()
    val statusDescription = varchar("status_description", 500).nullable()
    val permanentStorageDate = varchar("permanent_storage_date", 50).nullable()
}

class KsefSessionInvoiceStatusEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<KsefSessionInvoiceStatusEntity>(KsefSessionInvoiceStatusTable)

    var invoice by InvoiceEntity referencedOn KsefSessionInvoiceStatusTable.invoice
    var referenceNumber by KsefSessionInvoiceStatusTable.referenceNumber
    var invoiceNumber by KsefSessionInvoiceStatusTable.invoiceNumber
    var ksefNumber by KsefSessionInvoiceStatusTable.ksefNumber
    var statusCode by KsefSessionInvoiceStatusTable.statusCode
    var statusDescription by KsefSessionInvoiceStatusTable.statusDescription
    var permanentStorageDate by KsefSessionInvoiceStatusTable.permanentStorageDate

    fun toDomain(): KsefSessionInvoiceStatus = KsefSessionInvoiceStatus(
        id = id.value,
        invoiceId = invoice.id.value,
        referenceNumber = referenceNumber,
        invoiceNumber = invoiceNumber,
        ksefNumber = ksefNumber,
        statusCode = statusCode,
        statusDescription = statusDescription,
        permanentStorageDate = permanentStorageDate,
    )
}

fun KsefSessionInvoiceStatusResponse.toDomain(invoiceId: Long, entityId: Long? = null): KsefSessionInvoiceStatus =
    KsefSessionInvoiceStatus(
        id = entityId,
        invoiceId = invoiceId,
        referenceNumber = referenceNumber,
        invoiceNumber = invoiceNumber,
        ksefNumber = ksefNumber,
        statusCode = status?.code,
        statusDescription = status?.description,
        permanentStorageDate = permanentStorageDate,
    )

fun KsefSessionInvoiceStatusEntity.applyFromResponse(response: KsefSessionInvoiceStatusResponse) {
    referenceNumber = response.referenceNumber
    invoiceNumber = response.invoiceNumber
    ksefNumber = response.ksefNumber
    statusCode = response.status?.code
    statusDescription = response.status?.description
    permanentStorageDate = response.permanentStorageDate
}

fun KsefSessionInvoiceStatus.toStatusInfo(): KsefStatusInfo? =
    statusCode?.let { KsefStatusInfo(code = it, description = statusDescription) }

fun KsefSessionInvoiceStatus.toResponse(): KsefSessionInvoiceStatusResponse =
    KsefSessionInvoiceStatusResponse(
        referenceNumber = referenceNumber,
        invoiceNumber = invoiceNumber,
        ksefNumber = ksefNumber,
        status = toStatusInfo(),
        permanentStorageDate = permanentStorageDate,
    )
