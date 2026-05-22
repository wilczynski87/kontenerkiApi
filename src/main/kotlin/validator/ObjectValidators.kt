package com.kontenery.validator

import com.kontenery.data.Client
import com.kontenery.data.Contract
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.InvoiceType
import com.kontenery.data.utils.endOfCurrentMonth
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.data.utils.startOfCurrentMonth
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory

// if validator return FALSE == no errors
data class ObjectValidators(
    val errorList: MutableList<ErrorMessage>
) {
    private val logger = LoggerFactory.getLogger(ObjectValidators::class.java)

    fun validateClient(client: Client): Boolean {
        var result = false
        if(client.id == null)  {
            errorList.add(InvoiceErrorMessage("no Client Id", "client name: ${client.getName()}"))
            logger.error("no Client Id for client: {}", client.getName())
            result = true
        }

        if(client.isActive == false) {
            errorList.add(
                InvoiceErrorMessage(
                    "Client is no longer ACTIVE",
                    "client id=${client.id}, name=${client.getName()}",
                    clientId = client.id,
                )
            )
            logger.error("Client is no longer ACTIVE, id: {}", client.id)
            result = true
        }

        return result
    }

    fun validateContractsList(contracts: List<Contract>, period: LocalDate, client: Client): Boolean {
        var resut = false
        if(contracts.isEmpty()) {
            errorList.add(InvoiceErrorMessage(
                title = "no contracts",
                message = "client id=${client.id}, name=${client.getName()}, no active contracts for period from: ${LocalDate.startOfCurrentMonth(period)} to: ${LocalDate.endOfCurrentMonth(period)}",
                clientId = client.id,
                period = period,
            ))
            logger.error("no contracts, for given client: ${client.getName()}")
            resut = true
        }

        return resut
    }

    fun validateInvoicesList(invoices: List<Invoice>, period: LocalDate, client: Client): Boolean {
        var resut = false
        if(invoices.any { it.type == InvoiceType.PERIODIC.name }) {
            val invoicesNumbers = invoices.filter { it.type == InvoiceType.PERIODIC.name }.map { it.invoiceNumber }
            // there are periodic invoices already created for this client, for this month
            val error = InvoiceErrorMessage(
                title = "periodic invoice already created",
                message = "there are periodic invoices: $invoicesNumbers, already created for this client: ${client.getName()}, for this period: $period",
                clientId = client.id,
                period = period
            )
            errorList.add(error)
            logger.error(error.toString())
            resut = true
        }

        return resut
    }
}