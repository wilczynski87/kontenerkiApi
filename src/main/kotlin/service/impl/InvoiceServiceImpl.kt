package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.Contract
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.model.invoice.Position
import com.kontenery.library.model.invoice.Subject
import com.kontenery.library.utils.endOfCurrentMonth
import com.kontenery.library.utils.now
import com.kontenery.library.utils.startOfCurrentMonth
import com.kontenery.model.invoice.InvoiceNumber
import com.kontenery.repository.BillRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.InvoiceService
import com.kontenery.service.ProductService
import io.ktor.util.*
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

object BillCurrentNumber {
    var billNumber: InvoiceNumber? = null
    fun addOne(): InvoiceNumber {
        billNumber?.number = billNumber?.number?.plus(1L)!!
        return billNumber as InvoiceNumber
    }
}

class InvoiceServiceImpl(
    private val invoiceRepo: InvoiceRepo,
    private val billRepo: BillRepo,
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
//        println("in createPeriodicInvoiceForClient: clientId: $clientId, period: $period, invoiceTitle: $invoiceTitle")
        // find client
        val client: Client = clientService.findClientById(clientId) ?: throw NullPointerException("There is no client, with given Id: $clientId")
        // check if client is active - else throw exception
        if(client.isActive == false) {
            log.error("Client is no longer ACTIVE, id: $clientId")
            return null
        }

        // find active contract, for given period
        val contracts: List<Contract> = if(period == null) contractService.getByClientId(clientId, true)
            else contractService.getByClientId(clientId, LocalDate.startOfCurrentMonth(period), LocalDate.endOfCurrentMonth(period))

        // Do not create invoice if no Contract
        if(contracts.isEmpty()) {
            log.info("No contracts for given client, id: $clientId")
            return null
        }

        // set Invoice date
//        val invoiceDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val invoiceDate: LocalDate = LocalDate.now()
        // set Account
        val addTax: Boolean = client.needInvoice()

        // create list of positions for invoice, from contracts
        val positions:List<Position> = contracts
            .map { Position.toPosition(it) }

        // Create Invoice or Bill

        val savedBill: Invoice? = if(addTax) {
            val currentInvoiceNumber: String = createInvoiceNumber()
            // create invoice
            val inv = Invoice(
                invoiceNumber = currentInvoiceNumber,
                invoiceTitle = setInvoiceTitle(true, invoiceTitle),
                invoiceDate = invoiceDate,
                seller = Subject.Seller.company(currentInvoiceNumber),
                customer = Subject.Customer.toCustomer(client, currentInvoiceNumber),
                products = positions,
                vatAmountSum = vatAmountSumCalculate(contracts),
                priceSum = priceSumCalculate(contracts),
                priceWithVatSum = grossPrice(contracts),
                paymentDay = invoiceDate.plus(14, DateTimeUnit.DAY),
                mainAccount = "50 1950 0001 2006 0023 6241 0001",
                invoiceSendToClient = null,
                vatApply = true,
            )
//            println("invoice: $inv")
            invoiceRepo.saveInvoice(inv)
        } else {
            val currentBillNumber: String = createBillNumber()
            val finalPrice: String = priceSumCalculate(contracts)
            // create invoice
            val bill = Invoice(
                invoiceNumber = currentBillNumber,
                invoiceTitle = setInvoiceTitle(false, invoiceTitle),
                invoiceDate = invoiceDate,
                seller = Subject.Seller.personal(currentBillNumber),
                customer = Subject.Customer.toCustomer(client, currentBillNumber),
                products = positions,
                vatAmountSum = "-",
                priceSum = finalPrice,
                priceWithVatSum = finalPrice,
                paymentDay = invoiceDate.plus(14, DateTimeUnit.DAY),
                mainAccount = "11 2490 1044 0000 4200 8845 2192",
                invoiceSendToClient = null,
                vatApply = false
            )
//            println("bill: $bill")
            billRepo.saveBill(bill)
        }
//        println("return invoice/bill: $savedInvoice")
        return savedBill
    }

    private suspend fun createInvoiceNumber():String {
        return if(InvoiceCurrentNumber.invoiceNumber == null) {
            // fetching las invoice number (or set up 0)
            val invoiceNumberString = invoiceRepo.getLastInvoiceNumber() ?: InvoiceNumber(0).toInvoiceNumberString()
            // assigning invoice number to Object
            InvoiceCurrentNumber.invoiceNumber = InvoiceNumber.toInvoiceNumber(invoiceNumberString)
            // adding 1 to number and returning String
            InvoiceCurrentNumber.addOne().toInvoiceNumberString()
        } else InvoiceCurrentNumber.addOne().toInvoiceNumberString()
    }

    private suspend fun createBillNumber():String {
        return if(BillCurrentNumber.billNumber == null) {
            // fetching las invoice number (or set up 0)
            val invoiceNumberString = invoiceRepo.getLastBillNumber() ?: InvoiceNumber(0).toInvoiceNumberString()
            // assigning invoice number to Object
            BillCurrentNumber.billNumber = InvoiceNumber.toInvoiceNumber(invoiceNumberString)
            // adding 1 to number and returning String
            BillCurrentNumber.addOne().toInvoiceNumberString()
        } else BillCurrentNumber.addOne().toInvoiceNumberString()
    }

    private fun setInvoiceTitle(addTax: Boolean, extraTitle:String?): String {
        return if(extraTitle.isNullOrBlank() || extraTitle.trim().toLowerCasePreservingASCIIRules() == "null")
                if(addTax) "Faktura Vat" else "Faktura imienna bez Vat"
            else extraTitle

    }
    private fun priceSumCalculate(constracts: List<Contract>): String {
        return constracts
            .filter { it.product != null && it.netPrice != null }
            .map { it.netPrice }
            .reduce {sum, price -> sum!!.plus(price!!) }
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.toPlainString() ?: throw IllegalArgumentException("priceSumCalculate() exception")
    }
    private fun vatAmountSumCalculate(contracts: List<Contract>): String {
        return contracts
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

    override suspend fun createCustomInvoice(invoice: Invoice): Invoice? {
        return if(invoice.vatApply) {
                invoiceRepo.saveInvoice(invoice)
            } else {
                billRepo.saveBill(invoice)
            }
    }

    override suspend fun createUtilitiesInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun confirmInvoiceSendDate(invoiceNumber: String, date: LocalDate): Boolean {
        return invoiceRepo.confirmInvoiceSendDate(invoiceNumber, date)
    }
}