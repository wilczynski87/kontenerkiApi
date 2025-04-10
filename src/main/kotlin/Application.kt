package com.kontenery

import com.kontenery.repository.AddressRepo
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.impl.AddressRepoImpl
import com.kontenery.repository.impl.ClientRepoImpl
import com.kontenery.service.AddressService
import com.kontenery.service.ClientService
import com.kontenery.service.impl.AddressServiceImpl
import com.kontenery.service.impl.ClientServiceImpl
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

    configureFrameworks()
    configureSerialization()
    configureDatabases()
    validator()
    configureHTTP()
    configureSecurity()
    configureRouting(addressService, clientService)
}
