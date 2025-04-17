package com.kontenery.repository.impl

import com.kontenery.model.Contract
import com.kontenery.repository.ContractRepo
import com.kontenery.repository.entity.*

class ContractRepoImpl: ContractRepo {
    override suspend fun findAll(page:Int, size:Int): List<Contract> = suspendTransaction {
        val offset:Long = (page * size).toLong()
        ContractEntity.all()
            .offset(offset)
            .limit(size)
            .map { it.toContract() }
    }

    override suspend fun findById(id: Long): Contract? = suspendTransaction {
        ContractEntity.findById(id)?.toContract()
    }

    override suspend fun findByClientId(clientId: Long): List<Contract> {
        return ContractEntity.find {
                ContractTable.client eq clientId
            }
            .map { it.toContract() }
    }

    override suspend fun create(contract: Contract): Contract = suspendTransaction {
        val entity = ContractEntity.new {
            client = contract.client?.id?.let { ClientEntity.findById(it) }
            product = contract.product?.id?.let { ProductEntity.findById(it) }
            startDate = contract.startDate
            endDate = contract.endDate
            netPrice = contract.netPrice
            vatRate = contract.vatRate
            needInvoice = contract.needInvoice
        }
        entity.toContract()
    }

    override suspend fun update(id: Long, contract: Contract): Contract = suspendTransaction {
        val entity = ContractEntity.findById(id)
            ?: throw NoSuchElementException("Contract with id $id not found")

        entity.apply {
            client = contract.client?.id?.let { ClientEntity.findById(it) }
            product = contract.product?.id?.let { ProductEntity.findById(it) }
            startDate = contract.startDate
            endDate = contract.endDate
            netPrice = contract.netPrice
            vatRate = contract.vatRate
            needInvoice = contract.needInvoice
        }

        entity.toContract()
    }

    override suspend fun delete(id: Long): Boolean = suspendTransaction {
        ContractEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }
}