package com.kontenery.validator

import com.kontenery.library.model.Client
import com.kontenery.library.model.Contract
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.InvoiceType
import com.kontenery.library.utils.endOfCurrentMonth
import com.kontenery.library.utils.errors.ErrorMessage
import com.kontenery.library.utils.errors.InvoiceErrorMessage
import com.kontenery.library.utils.startOfCurrentMonth
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory

// if validator return FALSE == no errors
data class ObjectValidators(
    val errorList: MutableList<ErrorMessage>
) {
    private val logger = LoggerFactory.getLogger(ObjectValidators::class.java)

    fun validateClient(client: Client): Boolean {
        var resut = false
        if(client.id == null)  {
            errorList.add(InvoiceErrorMessage("no Client Id", "for Client: $client"))
            logger.error("no Client Id")
            resut = true
        }

        if(client.isActive == false) {
            errorList.add(InvoiceErrorMessage("Client is no longer ACTIVE", "for Client: $client"))
            logger.error("Client is no longer ACTIVE, id: ${client.id ?: client.getName()}")
            resut = true
        }

        return resut
    }

    fun validateContractsList(contracts: List<Contract>, period: LocalDate, client: Client): Boolean {
        var resut = false
        if(contracts.isEmpty()) {
            errorList.add(InvoiceErrorMessage(
                "no contracts",
                "for Client: $client, there are no active contracts, for period from: ${LocalDate.startOfCurrentMonth(period)} to: ${LocalDate.endOfCurrentMonth(period)}")
            )
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