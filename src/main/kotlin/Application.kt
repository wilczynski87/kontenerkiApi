package com.kontenery

import com.kontenery.repository.*
import com.kontenery.repository.impl.*
import com.kontenery.service.*
import com.kontenery.service.impl.*
import com.kontenery.validator.validator
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    println("App MAIN API started")

    val addressRepo: AddressRepo = AddressRepoImpl()
    val addressService: AddressService = AddressServiceImpl(addressRepo)

    val clientRepo:ClientRepo = ClientRepoImpl(addressRepo)
    val clientService:ClientService = ClientServiceImpl(clientRepo)

    val productRepo: ProductRepo = ProductRepoImpl()
    val productService: ProductService = ProductServiceImp(productRepo)

    val contractRepo: ContractRepo = ContractRepoImpl()
    val contractService: ContractService = ContractServiceImpl(contractRepo, clientService, productService)

    val invoiceRepo: InvoiceRepo = InvoiceRepoImpl()
    val billRepo: BillRepo = BillRepoImpl()
    val invoiceService: InvoiceService = InvoiceServiceImpl(invoiceRepo, billRepo, clientService, productService, contractService)

    val printService:PrintService = PrintServiceImpl()

    val paymentRepo:PaymentRepo = PaymentRepoImpl()
    val paymentService:PaymentService = PaymentServiceImpl(paymentRepo, clientService, invoiceService)

    val clientBankAccountRepository: ClientBankAccountRepository = ClientBankAccountRepositoryImpl()
    val bankAccountService: BankAccountService = BankAccountServiceImpl(clientBankAccountRepository)

    val csvService: CSVService = CSVServiceImpl(bankAccountService)

    val listingService: ListingService = ListingServiceImpl(clientRepo, productRepo, contractRepo, paymentRepo, invoiceRepo, billRepo)

    logger()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    validator(contractService)
    configureHTTP()
    configureSecurity()
    configureRouting(addressService, clientService, productService, contractService, invoiceService, printService, paymentService, csvService, bankAccountService, listingService)
}
