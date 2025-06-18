package com.kontenery.service.impl

import com.kontenery.library.model.Address
import com.kontenery.repository.AddressRepo
import com.kontenery.service.AddressService
import org.jetbrains.exposed.sql.SizedIterable

class AddressServiceImpl(
    private val addressRepo: AddressRepo,
): AddressService {
    override suspend fun save(address: Address): Address? {
        return addressRepo.save(address)
    }

    override suspend fun findAll(page: Int, size: Int): List<Address> {
        return addressRepo.findAll(page, size)
    }

    override suspend fun findById(id: Long): Address? {
        return addressRepo.findById(id)
    }

}