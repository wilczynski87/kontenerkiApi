package com.kontenery.service.impl

import com.kontenery.model.Client
import com.kontenery.model.Contract
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.invoice.InvoiceNumber
import com.kontenery.model.invoice.Position
import com.kontenery.model.invoice.Subject
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.endOfCurrentMonth
import com.kontenery.repository.startOfCurrentMonth
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.InvoiceService
import com.kontenery.service.ProductService
import kotlinx.datetime.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

object InvoiceCurrentNumber {
    var invoiceNumber: InvoiceNumber? = null
    fun addOne(): InvoiceNumber {
        invoiceNumber?.number = invoiceNumber?.number?.plus(1L)!!
        return invoiceNumber as InvoiceNumber
    }
}

class InvoiceServiceImpl(
    private val invoiceRepo: InvoiceRepo,
    private val clientService: ClientService,
    private val productService: ProductService,
    private val contractService: ContractService
): InvoiceService {
    private val log = LoggerFactory.getLogger(InvoiceServiceImpl::class.java)

    override suspend fun getInvoicesForDate(page: Int, size: Int, from: LocalDate, to: LocalDate): List<Invoice> {
        return invoiceRepo.getInvoicesForDate(page, size, from, to)
    }

    override suspend fun getInvoicesForClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Invoice> {
        return invoiceRepo.getInvoicesForClient(page, size, clientId, from, to)
    }

    override suspend fun getInvoiceById(invoiceId: Long): Invoice? {
        return invoiceRepo.getInvoiceById(invoiceId)
    }

    override suspend fun saveInvoice(invoice: Invoice): Invoice? {
        return invoiceRepo.saveInvoice(invoice)
    }

    /*
    TO DO:
    - funkcja licząca invoice nie tylko za bierzący okres
     */
    override suspend fun createPeriodicInvoiceForClient(clientId: Long, period: LocalDate?, invoiceTitle: String?): Invoice? {
        // find client
        val client:Client = clientService.findClientById(clientId) ?: throw NullPointerException("There is no client, with given Id: $clientId")
        // check if client is active - else throw exception
        if(client.isActive == false) {
            log.error("Client is no longer ACTIVE, id: $clientId")
            return null
        }

        // find active contract, for given period
        val contracts: List<Contract> = if(period == null) contractService.getByClientId(clientId, true)
        else contractService.getByClientId(clientId, LocalDate.startOfCurrentMonth(period), LocalDate.endOfCurrentMonth(period))

        // Do not create invoice if no Contract
        if(contracts.isEmpty()) return null

        // set Invoice date
        val invoiceDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        // set Account
        val addTax: Boolean = client.needInvoice()
        val account: String = if(addTax) "50 1950 0001 2006 0023 6241 0001" else "11 2490 1044 0000 4200 8845 2192"
        // set invoice number
        val currentInvoiceNumber: String = getInvoiceNumber()

        // create list of positions for invoice, from contracts
        val positions:List<Position> = contracts
            .filter { it.product != null }
            .map { Position.toPosition(it) }

        // create invoice
        val invoice = Invoice(
            invoiceNumber = currentInvoiceNumber,
            invoiceTitle = setInvoiceTite(addTax, invoiceTitle),
            invoiceDate = invoiceDate,
            seller = Subject.Seller(invoiceNumber = currentInvoiceNumber),
            customer = Subject.Customer.toCustomer(client, currentInvoiceNumber),
            products = positions,
            vatAmountSum = vatAmountSumCalculate(contracts),
            priceSum = priceSumCalculate(contracts),
            priceWithVatSum = grossPrice(contracts),
            paymentDay = invoiceDate.plus(14, DateTimeUnit.DAY),
            mainAccount = account,
            invoiceSendToClient = null,
        )

        // save invoice in db
        val savedInvoice:Invoice? = invoiceRepo.saveInvoice(invoice)

        // send invoice to Client
        // TODO

        return savedInvoice
    }

    private suspend fun getInvoiceNumber():String {
        return if(InvoiceCurrentNumber.invoiceNumber == null) {
            // fetching las invoice number (or set up 0)
            val invoiceNumberString = invoiceRepo.getLastInvoiceNumber() ?: InvoiceNumber(0).toInvoiceNumberString()
            // assigning invoice number to Object
            InvoiceCurrentNumber.invoiceNumber = InvoiceNumber.toInvoiceNumber(invoiceNumberString)
            // adding 1 to number and returning String
            InvoiceCurrentNumber.addOne().toInvoiceNumberString()
        } else InvoiceCurrentNumber.addOne().toInvoiceNumberString()
    }
    private fun setInvoiceTite(addTax: Boolean, extraTitle:String?): String {
        return if(extraTitle.isNullOrBlank())
                if(addTax) "Faktura Vat" else "Faktura bez Vat"
            else extraTitle

    }
    private fun priceSumCalculate(constracts: List<Contract>): String {
        return constracts
            .filter { it.product != null && it.netPrice != null }
            .map { it.netPrice }
            .reduce {sum, price -> sum!!.plus(price!!) }
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.toPlainString() ?: throw IllegalArgumentException("priceSumCalculate() exception")
        // zaokrąglenie!

    }
    private fun vatAmountSumCalculate(constracts: List<Contract>): String {
        return constracts
            .filter { it.product != null && it.netPrice != null }
            .map { it.netPrice!!.multiply(it.vatRate.divide(BigDecimal(100),2, RoundingMode.HALF_UP)) }
            .reduce {sum, price -> sum!!.plus(price!!) }
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }
    private fun grossPrice(constracts: List<Contract>): String {
        return constracts
            .filter { it.product != null && it.netPrice != null }
            .map { it.netPrice!!.multiply((it.vatRate.plus(BigDecimal(100))).divide(BigDecimal(100),2, RoundingMode.HALF_UP)) }
            .reduce {sum, price -> sum!!.plus(price!!) }
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }

    override suspend fun createPeriodicInvoiceForAllClients(period: LocalDate?): List<Invoice> {
        return clientService.getAllClients(0, 1000)
            .mapNotNull { it.id }
            .mapNotNull { createPeriodicInvoiceForClient(it, period) }

    }

    override suspend fun createCustomInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun createUtilitiesInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun confirmInvoiceSendDate(invoiceNumber: String, date: LocalDate): Boolean {
        return invoiceRepo.confirmInvoiceSendDate(invoiceNumber, date)
    }
}