package com.kontenery.repository.impl

import com.kontenery.model.Contract
import com.kontenery.repository.ContractRepo
import com.kontenery.repository.entity.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

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

    override suspend fun findByClientId(clientId: Long, onlyActive: Boolean): List<Contract> = suspendTransaction {
        if(onlyActive) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            ContractEntity.find {
                (ContractTable.client eq clientId) and
                (
                    ContractTable.endDate.isNull() or
                    (ContractTable.endDate greater now)
                )
            }.map { it.toContract() }
        } else {
            ContractEntity.find {
                ContractTable.client eq clientId
            }.map { it.toContract() }
        }
    }

    override suspend fun findByClientId(clientId: Long, fromDate: LocalDate, toDate: LocalDate): List<Contract> = suspendTransaction {
        ContractEntity.find {
            (ContractTable.client eq clientId) and
            (
                (ContractTable.endDate.isNull() or (ContractTable.endDate greater toDate)) and
                (ContractTable.startDate lessEq fromDate)
            )
        }.map { it.toContract() }
    }

    override suspend fun findCurrentByProductId(productId: Long): List<Contract> = suspendTransaction {
        val today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        ContractEntity.find {
            (ContractTable.product eq productId) and ((ContractTable.endDate.isNull()) or (ContractTable.endDate greater today))
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