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
import com.kontenery.service.BankAccountService
import com.kontenery.service.CSVService
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.InvoiceService
import com.kontenery.service.ListingService
import com.kontenery.service.PaymentService
import com.kontenery.service.PrintService
import com.kontenery.service.ProductService
import com.kontenery.service.UtilitiesService
import com.kontenery.service.impl.AddressServiceImpl
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
import com.kontenery.validator.validator
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.log

fun main(args: Array<String>) {
    if (System.getenv("API_ENV") == null) {
        dotenv {
            directory = "."
            filename = "api.env"
            ignoreIfMissing = true
            systemProperties = true
        }
    }
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    println("App MAIN API started")
    val apiConfig: ApiConfig = loadApiConfig()
    log.info("Environment: ${apiConfig.env}")


//    val config = environment.config
//    val production = config.propertyOrNull("api.ENV")?.getString() ?: "DEV"
//    println("production is: $production")
//    // PROD vs DEV
//    var emailHost: String
//    var emailPort: String
//    var dbHost: String
//    var dbPort: String
//    var dbPassword: String
//
//    if(production.equals(Env.PROD.name, ignoreCase = true)) {
//        println("PRODUCTION")
//
//        emailHost = config.propertyOrNull("api.EMAIL_HOST")?.getString()
//            ?: throw NullPointerException("There is no email address")
//        emailPort = config.propertyOrNull("api.EMAIL_PORT")?.getString()
//            ?: throw NullPointerException("There is no email port")
//        dbHost = config.propertyOrNull("api.DB_HOST")?.getString()
//            ?: throw NullPointerException("There is no DB_HOST")
//        dbPort = config.propertyOrNull("api.DB_PORT")?.getString()
//            ?: throw NullPointerException("There is no DB_PORT")
//        dbPassword = config.propertyOrNull("api.DB_PASSWORD")?.getString()
//            ?: throw NullPointerException("There is no DB_PASSWORD")
//
//    } else {
//        println("DEVELOPEMENT")
//
//        val env = dotenv {
//            directory = "."
//            filename = "api.env"
//            ignoreIfMissing = false
//            systemProperties = true
//        }
//
//        emailHost = env["EMAIL_HOST"]
//            ?: throw NullPointerException("There is no email address")
//        emailPort = env["EMAIL_PORT"]
//            ?: throw NullPointerException("There is no email port")
//        dbHost = env["DB_HOST"]
//            ?: throw NullPointerException("There is no DB_HOST")
//        dbPort = env["DB_PORT"]
//            ?: throw NullPointerException("There is no DB_PORT")
//        dbPassword = env["DB_PASSWORD"]
//            ?: throw NullPointerException("There is no DB_PASSWORD")
//    }

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

    val printService:PrintService = PrintServiceImpl(apiConfig.email.host, apiConfig.email.port.toString())

    val paymentRepo:PaymentRepo = PaymentRepoImpl()
    val paymentService:PaymentService = PaymentServiceImpl(paymentRepo, clientService, invoiceService)

    val clientBankAccountRepository: ClientBankAccountRepository = ClientBankAccountRepositoryImpl()
    val bankAccountService: BankAccountService = BankAccountServiceImpl(clientBankAccountRepository)

    val csvService: CSVService = CSVServiceImpl(bankAccountService)

    val listingService: ListingService = ListingServiceImpl(clientRepo, productRepo, contractRepo, paymentRepo, invoiceRepo, billRepo)

    val utilitiesRepo: UtilitiesRepo = UtilitiesRepoImpl()
    val utilitiesService: UtilitiesService = UtilitiesServiceImpl(utilitiesRepo)

    logger()
    configureFrameworks()
    configureSerialization()
    configureDatabases(apiConfig)
    validator(contractService)
    configureHTTP()
    configureSecurity()
    configureRouting(addressService, clientService, productService, contractService, invoiceService, printService, paymentService, csvService, bankAccountService, listingService, utilitiesService)
}
