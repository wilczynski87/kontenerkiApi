package com.kontenery.service

import com.kontenery.data.Address

interface AddressService {

    suspend fun save(address: Address): Address?

    suspend fun findAll(page: Int, size: Int): List<Address>

    suspend fun findById(id: Long): Address?
}