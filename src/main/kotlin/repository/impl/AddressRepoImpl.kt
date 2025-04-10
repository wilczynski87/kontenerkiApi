package com.kontenery.repository.impl

import com.kontenery.model.Address
import com.kontenery.repository.entity.AddressDAO
import com.kontenery.repository.AddressRepo
import com.kontenery.repository.entity.suspendTransaction
import com.kontenery.repository.entity.toAddress
import org.jetbrains.exposed.sql.mapLazy

class AddressRepoImpl: AddressRepo {
    override suspend fun save(address: Address): Address {

        return suspendTransaction {
            AddressDAO.new {
                street = address.street
                house = address.house
                city = address.city
                country = address.country
                postcode = address.postCode
            }.toAddress()
        }
    }

    override suspend fun saveToDao(address: Address): AddressDAO {
        return suspendTransaction {
            AddressDAO.new {
                street = address.street
                house = address.house
                city = address.city
                country = address.country
                postcode = address.postCode
            }
        }
    }

    override suspend fun findAll(page: Int, size: Int): List<Address> {
        return suspendTransaction {
            val countOffset:Long = (page * size).toLong()
            AddressDAO.all()
                .limit(size)
                .offset(countOffset)
                .mapLazy { it.toAddress() }
                .toList()
        }
    }

    override suspend fun findById(id: Long): Address? {
        return suspendTransaction {
            AddressDAO.findById(id)?.toAddress()
        }
    }
}