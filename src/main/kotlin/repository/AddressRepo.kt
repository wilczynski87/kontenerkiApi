package com.kontenery.repository

import com.kontenery.model.Address
import com.kontenery.repository.entity.AddressDAO
import org.jetbrains.exposed.sql.SizedIterable

interface AddressRepo {

    suspend fun save(address: Address): Address?

    suspend fun saveToDao(address: Address): AddressDAO

    suspend fun findAll(page: Int, size: Int): List<Address>

    suspend fun findById(id: Long): Address?
}