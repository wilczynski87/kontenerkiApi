package com.kontenery.service

import com.kontenery.library.model.Contract
import com.kontenery.library.model.ContractDto
import kotlinx.datetime.LocalDate

interface ContractService {

    suspend fun getAll(page:Int, size:Int): List<Contract>
    suspend fun getById(id: Long): Contract?
    suspend fun getByClientId(clientId: Long, onlyActive:Boolean = false): List<Contract>
    suspend fun getByClientId(clientId: Long, fromDate:LocalDate, toDate: LocalDate): List<Contract>
    suspend fun getCurrentByProductId(productId: Long): Contract?
    suspend fun create(contractDto: ContractDto): Contract
    suspend fun save(contract: Contract): Contract?
    suspend fun update(contractId: Long, contractDto: ContractDto): Contract
    suspend fun delete(id: Long): Boolean

}