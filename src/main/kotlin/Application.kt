package com.kontenery

import com.kontenery.model.Product
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

    val addressRepo: AddressRepo = AddressRepoImpl()
    val addressService: AddressService = AddressServiceImpl(addressRepo)

    val clientRepo:ClientRepo = ClientRepoImpl(addressRepo)
    val clientService:ClientService = ClientServiceImpl(clientRepo)

    val productRepo: ProductRepo = ProductRepoImpl()
    val productService: ProductService = ProductServiceImp(productRepo)

    val contractRepo: ContractRepo = ContractRepoImpl()
    val contractService: ContractService = ContractServiceImpl(contractRepo, clientService, productService)

    val invoiceRepo: InvoiceRepo = InvoiceRepoImpl()
    val invoiceService: InvoiceService = InvoiceServiceImpl(invoiceRepo)

    configureFrameworks()
    configureSerialization()
    configureDatabases()
    validator(contractService)
    configureHTTP()
    configureSecurity()
    configureRouting(addressService, clientService, productService, contractService, invoiceService)
}
