package com.kontenery.repository

import com.kontenery.library.model.Address
import com.kontenery.repository.entity.AddressDAO

interface AddressRepo {

    suspend fun save(address: Address): Address?

    suspend fun saveToDao(address: Address): AddressDAO

    suspend fun findAll(page: Int, size: Int): List<Address>

    suspend fun findById(id: Long): Address?
}