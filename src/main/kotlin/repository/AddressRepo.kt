package com.kontenery.repository

import com.kontenery.library.model.Address
import com.kontenery.repository.entity.AddressEntity

interface AddressRepo {

    suspend fun save(address: Address): Address?

    suspend fun saveToDao(address: Address): AddressEntity

    suspend fun findAll(page: Int, size: Int): List<Address>

    suspend fun findById(id: Long): Address?
}