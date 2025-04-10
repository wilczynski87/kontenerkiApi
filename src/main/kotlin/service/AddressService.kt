package com.kontenery.service

import com.kontenery.model.Address
import com.kontenery.repository.AddressRepo
import org.jetbrains.exposed.sql.SizedIterable

interface AddressService {

    suspend fun save(address: Address): Address?

    suspend fun findAll(page: Int, size: Int): List<Address>

    suspend fun findById(id: Long): Address?
}