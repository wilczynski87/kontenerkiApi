package com.kontenery

import com.kontenery.repository.AddressRepo
import com.kontenery.repository.BillRepo
import com.kontenery.repository.ClientBankAccountRepository
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.ContractRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.PaymentRepo
import com.kontenery.repository.ProductRepo
import com.kontenery.repository.UtilitiesRepo
import com.kontenery.repository.impl.AddressRepoImpl
import com.kontenery.repository.impl.BillRepoImpl
import com.kontenery.repository.impl.ClientBankAccountRepositoryImpl
import com.kontenery.repository.impl.ClientRepoImpl
import com.kontenery.repository.impl.ContractRepoImpl
import com.kontenery.repository.impl.InvoiceRepoImpl
import com.kontenery.repository.impl.PaymentRepoImpl
import com.kontenery.repository.impl.ProductRepoImpl
import com.kontenery.repository.impl.UtilitiesRepoImpl
import com.kontenery.service.AddressService
import com.kontenery.service.AuthService
import com.kontenery.service.BankAccountService
import com.kontenery.service.CSVService
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.InvoiceService
import com.kontenery.service.JwtConfig
import com.kontenery.service.ListingService
import com.kontenery.service.PaymentService
import com.kontenery.service.PrintService
import com.kontenery.service.ProductService
import com.kontenery.service.UtilitiesService
import com.kontenery.service.impl.AddressServiceImpl
import com.kontenery.service.impl.AuthServiceImpl
import com.kontenery.service.impl.BankAccountServiceImpl
import com.kontenery.service.impl.CSVServiceImpl
import com.kontenery.service.impl.ClientServiceImpl
import com.kontenery.service.impl.ContractServiceImpl
import com.kontenery.service.impl.InvoiceServiceImpl
import com.kontenery.service.impl.ListingServiceImpl
import com.kontenery.service.impl.PaymentServiceImpl
import com.kontenery.service.impl.PrintServiceImpl
import com.kontenery.service.impl.ProductServiceImp
import com.kontenery.service.impl.UtilitiesServiceImpl
import com.kontenery.validator.PaymentValidator
import com.kontenery.validator.httpValidator
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
//    io.ktor.server.netty.EngineMain.main(args)
    embeddedServer(Netty, port = System.getenv("API_PORT")?.toInt() ?: 8100) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    println("\nMAIN API started\n")
    val apiConfig: ApiConfig = loadApiConfig()
    log.info("Environment: ${apiConfig.env}")
    val paymentRepo:PaymentRepo = PaymentRepoImpl()
    val invoiceRepo: InvoiceRepo = InvoiceRepoImpl()
    val billRepo: BillRepo = BillRepoImpl()

    val addressRepo: AddressRepo = AddressRepoImpl()
    val addressService: AddressService = AddressServiceImpl(addressRepo)

    val clientRepo:ClientRepo = ClientRepoImpl(addressRepo)
    val clientService:ClientService = ClientServiceImpl(clientRepo, paymentRepo, invoiceRepo, billRepo)

    val productRepo: ProductRepo = ProductRepoImpl()
    val productService: ProductService = ProductServiceImp(productRepo)

    val contractRepo: ContractRepo = ContractRepoImpl()
    val contractService: ContractService = ContractServiceImpl(contractRepo, clientService, productService)

    val invoiceService: InvoiceService = InvoiceServiceImpl(invoiceRepo, billRepo, clientService, productService, contractService)

    val printService:PrintService = PrintServiceImpl(apiConfig.email.host, apiConfig.email.port.toString())

    val paymentService:PaymentService = PaymentServiceImpl(paymentRepo, clientService, invoiceService)

    val clientBankAccountRepository: ClientBankAccountRepository = ClientBankAccountRepositoryImpl()
    val bankAccountService: BankAccountService = BankAccountServiceImpl(clientBankAccountRepository)

    val csvService: CSVService = CSVServiceImpl(bankAccountService)

    val paymentValidator = PaymentValidator(paymentRepo)

    val listingService: ListingService = ListingServiceImpl(clientRepo, productRepo, contractRepo, paymentRepo, invoiceRepo, billRepo)

    val utilitiesRepo: UtilitiesRepo = UtilitiesRepoImpl()
    val utilitiesService: UtilitiesService = UtilitiesServiceImpl(utilitiesRepo)

    val jwtConfig = JwtConfig(apiConfig)
    val authService: AuthService = AuthServiceImpl(jwtConfig)


    logger()
    configureFrameworks()
    configureSerialization()
    configureDatabases(apiConfig)
    httpValidator(contractService)
    configureSecurity(jwtConfig)
    configureHTTP()
    configureRouting(addressService, clientService, productService, contractService, invoiceService, printService, paymentService, csvService, bankAccountService, listingService, utilitiesService, authService, paymentValidator)
}
