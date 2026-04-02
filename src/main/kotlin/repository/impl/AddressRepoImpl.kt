package com.kontenery.repository.impl

import com.kontenery.data.Address
import com.kontenery.repository.entity.AddressEntity
import com.kontenery.repository.AddressRepo
import com.kontenery.repository.entity.suspendTransaction
import com.kontenery.repository.entity.toAddress
import org.jetbrains.exposed.sql.mapLazy

class AddressRepoImpl: AddressRepo {
    override suspend fun save(address: Address): Address {

        return suspendTransaction {
            AddressEntity.new {
                street = address.street
                house = address.house
                city = address.city
                country = address.country
                postCode = address.postCode
            }.toAddress()
        }
    }

    override suspend fun saveToDao(address: Address): AddressEntity {
        return suspendTransaction {
            AddressEntity.new {
                street = address.street
                house = address.house
                city = address.city
                country = address.country
                postCode = address.postCode
            }
        }
    }

    override suspend fun findAll(page: Int, size: Int): List<Address> {
        return suspendTransaction {
            val countOffset:Long = (page * size).toLong()
            AddressEntity.all()
                .limit(size)
                .offset(countOffset)
                .mapLazy { it.toAddress() }
                .toList()
        }
    }

    override suspend fun findById(id: Long): Address? {
        return suspendTransaction {
            AddressEntity.findById(id)?.toAddress()
        }
    }
}