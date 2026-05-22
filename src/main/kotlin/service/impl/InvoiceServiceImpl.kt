package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.Contract
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Position
import com.kontenery.data.invoice.Subject
import com.kontenery.data.utils.InvoiceType
import com.kontenery.data.utils.endOfCurrentMonth
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.data.utils.now
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.model.invoice.InvoiceNumber
import com.kontenery.repository.BillRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.InvoiceService
import com.kontenery.service.ProductService
import com.kontenery.validator.ObjectValidators
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

    override suspend fun getInvoicesAndBillsForClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Invoice> {
        return invoiceRepo.getInvoicesForClient(page, size, clientId, from, to) + billRepo.getBillsForClient(page, size, clientId, from, to)
    }

    override suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice? {
        return if(invoiceNumber.trim().endsWith("r")) billRepo.getBillByNumber(invoiceNumber)
            else invoiceRepo.getInvoiceByNumber(invoiceNumber)
    }

    override suspend fun saveInvoice(invoice: Invoice): Invoice? {
        return invoiceRepo.saveInvoice(invoice)
    }

    override suspend fun saveInvoiceWithErrors(isInvoice: Boolean, invoice: Invoice, errors: MutableList<ErrorMessage>): Invoice? {
        return try {
            if(isInvoice) invoiceRepo.saveInvoice(invoice)
            else billRepo.saveBill(invoice)
        } catch (e: Exception) {
            log.error("Problem z zapisem faktury: ${invoice.invoiceNumber}," +
                "dla klienta: ${invoice.customer?.client?.getName()}" +
                "\n $e" +
                "\n $invoice"
            )
            errors.add(
                InvoiceErrorMessage(
                    title = "błąd zapisu faktury ${invoice.invoiceNumber}",
                    message = "błąd zapisu faktury, dla klienta: ${invoice.customer?.client?.getName()}",
                    clientId = invoice.customer?.client?.id,
                    period = invoice.invoiceDate
                )
            )
            null
        }
    }

    /*
    TO DO:
    - funkcja licząca invoice nie tylko za bierzący okres
     */
//    override suspend fun createPeriodicInvoiceForClient(clientId: Long, period: LocalDate, invoiceTitle: String?): Invoice {
////         println("in createPeriodicInvoiceForClient: clientId: $clientId, period: $period, invoiceTitle: $invoiceTitle")
//        // find client
//        val client: Client = clientService.findClientById(clientId)
//            ?: throw NullPointerException("There is no client, with given Id: $clientId")
//        // check if client is active - else throw exception
//        if(client.isActive == false) {
//            log.error("Client is no longer ACTIVE, id: $clientId")
//            throw NullPointerException("Client is no longer ACTIVE, id: $clientId")
//        }
//
//        // find active contract, for given period
//        val contracts: List<Contract> =
//            contractService.getByClientId(clientId, LocalDate.startOfCurrentMonth(period), LocalDate.endOfCurrentMonth(period))
//
//        // Do not create invoice if no Contract
//        if(contracts.isEmpty()) {
//            log.info("No contracts for given client, id: $clientId")
//            throw NullPointerException("No contracts for given client, id: $clientId")
//        }
//
//        // set Invoice date
//        val invoiceDate: LocalDate = LocalDate.now()
//        // set Account
//        val addTax: Boolean = client.needInvoice()
//
//        // create list of positions for invoice, from contracts
//        val positions:List<Position> = contracts
//            .map { Position.toPosition(it) }
//
//        // Create Invoice or Bill
////        println("before creating bill:")
//        val savedBill: Invoice = if(addTax) {
//            val currentInvoiceNumber: String = createInvoiceNumber()
//            // create invoice
//            val inv = Invoice(
//                invoiceNumber = currentInvoiceNumber,
//                invoiceTitle = setInvoiceTitle(invoiceTitle, client.invoiceTitle),
//                invoiceDate = invoiceDate,
//                seller = Subject.Seller.company(currentInvoiceNumber),
//                customer = Subject.Customer.toCustomer(client, currentInvoiceNumber),
//                products = positions,
//                vatAmountSum = vatAmountSumCalculate(contracts),
//                priceSum = priceSumCalculate(contracts),
//                priceWithVatSum = grossPrice(contracts),
//                paymentDay = invoiceDate.plus(14, DateTimeUnit.DAY),
//                mainAccount = "50 1950 0001 2006 0023 6241 0001",
//                invoiceSendToClient = null,
//                vatApply = true,
//                type = InvoiceType.PERIODIC.name
//            )
////            println("invoice: $inv")
//            invoiceRepo.saveInvoice(inv) ?: throw NullPointerException("Can not create invoice: $inv")
//        } else {
//            val currentBillNumber: String = createBillNumber()
//            val finalPrice: String = priceSumCalculate(contracts)
//            // create invoice
//            val bill = Invoice(
//                invoiceNumber = currentBillNumber,
//                invoiceTitle = setInvoiceTitle(invoiceTitle, client.invoiceTitle),
//                invoiceDate = invoiceDate,
//                seller = Subject.Seller.personal(currentBillNumber),
//                customer = Subject.Customer.toCustomer(client, currentBillNumber),
//                products = positions,
//                vatAmountSum = "-",
//                priceSum = finalPrice,
//                priceWithVatSum = finalPrice,
//                paymentDay = invoiceDate.plus(14, DateTimeUnit.DAY),
//                mainAccount = "11 2490 1044 0000 4200 8845 2192",
//                invoiceSendToClient = null,
//                vatApply = false,
//                type = InvoiceType.PERIODIC.name
//            )
////            println("bill: $bill")
//            billRepo.saveBill(bill) ?: throw NullPointerException("Can not create bill: $bill")
//        }
////        println("return invoice/bill: $savedInvoice")
//        return savedBill
//    }

//    private suspend fun createInvoiceNumber():String {
//        return if(InvoiceCurrentNumber.invoiceNumber == null) {
//            // fetching las invoice number (or set up 0)
//            val invoiceNumberString = invoiceRepo.getLastInvoiceNumber() ?: InvoiceNumber(0).toInvoiceNumberString()
//            // assigning invoice number to Object
//            InvoiceCurrentNumber.invoiceNumber = InvoiceNumber.toInvoiceNumber(invoiceNumberString)
//            // adding 1 to number and returning String
//            InvoiceCurrentNumber.addOne().toInvoiceNumberString()
//        } else InvoiceCurrentNumber.addOne().toInvoiceNumberString()
//    }

    private fun setInvoiceTitle(invoiceTitle:String? = null, clientInvoiceTitle:String? = null): String? {
        return if(invoiceTitle.isNullOrBlank() || invoiceTitle.trim().toLowerCasePreservingASCIIRules() == "null") {
            clientInvoiceTitle
        } else invoiceTitle
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

    override suspend fun createPeriodicInvoiceForClient(client: Client, period: LocalDate?, errorList: MutableList<ErrorMessage>): Invoice? {
        val period = period ?: LocalDate.now()
        val validator = ObjectValidators(errorList)

        println("createPeriodicInvoiceForClient: ${client.getName()}")

        if(validator.validateClient(client)) return null
        val clientId: Long = client.id!!

        // check active contracts:
        val contracts: List<Contract> = contractService.getByClientId(
                clientId,
                LocalDate.startOfCurrentMonth(period),
                LocalDate.endOfCurrentMonth(period)
            )

        if(validator.validateContractsList(contracts, period, client)) return null

        val addTax: Boolean = client.needInvoice()

        // check if periodic invoice has been created
        val invoices: List<Invoice> = if(addTax) invoiceRepo.getInvoicesForClient(
                0,
                100,
                clientId,
                LocalDate.startOfCurrentMonth(period),
                LocalDate.endOfCurrentMonth(period)
            ) else billRepo.getBillsForClient(
                0,
                100,
                clientId,
                LocalDate.startOfCurrentMonth(period),
                LocalDate.endOfCurrentMonth(period)
            )

        if(validator.validateInvoicesList(invoices, period, client)) return null

        val positions:List<Position> = contracts
            .map { Position.toPosition(it) }

        val document: Invoice = if(addTax) createInvoice(client, period, contracts, positions)
            else createBill(client, period, contracts, positions)
//        println("document: $document")

        return document
    }

    override suspend fun createCustomInvoice(invoice: Invoice): Invoice? {
        return try {
            if(invoice.vatApply) {
                val invoiceWithNumber: Invoice = invoice.copy(
                    invoiceNumber = createInvoiceNumber()
                )
                invoiceWithNumber
//                invoiceRepo.saveInvoice(invoiceWithNumber)
            } else {
                val billWithNumber: Invoice = invoice.copy(
                    invoiceNumber = createBillNumber()
                )
                billWithNumber
//                billRepo.saveBill(billWithNumber)
            }
        } catch (e: Exception) {
            println()
            log.error("createCustomInvoice: $e")
            null
        }
    }

    override suspend fun createUtilitiesInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun confirmInvoiceSendDate(invoiceNumber: String, date: LocalDate): Boolean {
        return invoiceRepo.confirmInvoiceSendDate(invoiceNumber, date)
    }

    private suspend fun createInvoice(
        client: Client
        , invoiceDate: LocalDate? = LocalDate.now()
        , contracts: List<Contract>
        , positions:List<Position>
    ): Invoice {

        val currentInvoiceNumber: String = createInvoiceNumber()
        // create invoice
        return Invoice(
            invoiceNumber = currentInvoiceNumber,
            invoiceTitle = setInvoiceTitle(client.invoiceTitle),
            invoiceDate = invoiceDate,
            seller = Subject.Seller.company(currentInvoiceNumber),
            customer = Subject.Customer.toCustomer(client, currentInvoiceNumber),
            products = positions,
            vatAmountSum = vatAmountSumCalculate(contracts),
            priceSum = priceSumCalculate(contracts),
            priceWithVatSum = grossPrice(contracts),
            paymentDay = invoiceDate!!.plus(14, DateTimeUnit.DAY),
            mainAccount = "50 1950 0001 2006 0023 6241 0001",
            invoiceSendToClient = null,
            vatApply = true,
            type = InvoiceType.PERIODIC.name
        )
    }

    private suspend fun createBill(
        client: Client
        , invoiceDate: LocalDate? = LocalDate.now()
        , contracts: List<Contract>
        , positions:List<Position>
    ): Invoice {

        val currentBillNumber: String = createBillNumber()
        val finalPrice: String = priceSumCalculate(contracts)
        // create invoice
        return Invoice(
            invoiceNumber = currentBillNumber,
            invoiceTitle = setInvoiceTitle(client.invoiceTitle),
            invoiceDate = invoiceDate,
            seller = Subject.Seller.personal(currentBillNumber),
            customer = Subject.Customer.toCustomer(client, currentBillNumber),
            products = positions,
            vatAmountSum = "-",
            priceSum = finalPrice,
            priceWithVatSum = finalPrice,
            paymentDay = invoiceDate!!.plus(14, DateTimeUnit.DAY),
            mainAccount = "11 2490 1044 0000 4200 8845 2192",
            invoiceSendToClient = null,
            vatApply = false,
            type = InvoiceType.PERIODIC.name
        )
    }

    private suspend fun createInvoiceNumber(): String {
        return if(InvoiceCurrentNumber.invoiceNumber == null) {
            // fetching las invoice number (or set up 0)
            val invoiceNumberString = invoiceRepo.getLastInvoiceNumber() ?: InvoiceNumber(0).toInvoiceNumberString()
            // assigning invoice number to Object
            InvoiceCurrentNumber.invoiceNumber = InvoiceNumber.toInvoiceNumber(invoiceNumberString)
            // adding 1 to number and returning String
            InvoiceCurrentNumber.addOne().toInvoiceNumberString()
        } else InvoiceCurrentNumber.addOne().toInvoiceNumberString()
    }
    private suspend fun createBillNumber(): String {
        return if(BillCurrentNumber.billNumber == null) {
            // fetching las invoice number (or set up 0)
            val invoiceNumberString = invoiceRepo.getLastBillNumber() ?: InvoiceNumber(0).toInvoiceNumberString()
            // assigning invoice number to Object
            BillCurrentNumber.billNumber = InvoiceNumber.toInvoiceNumber(invoiceNumberString)
            // adding 1 to number and returning String
            BillCurrentNumber.addOne().toInvoiceNumberString()
                .run { if(endsWith('r')) this else this + "r" }
        } else BillCurrentNumber.addOne().toInvoiceNumberString()
            .run { if(endsWith('r')) this else this + "r" }
    }
}