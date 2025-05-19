package com.kontenery.service

import com.kontenery.model.Contract
import com.kontenery.model.ContractDto
import kotlinx.datetime.LocalDate

interface ContractService {

    suspend fun getAll(page:Int, size:Int): List<Contract>
    suspend fun getById(id: Long): Contract?
    suspend fun getByClientId(clientId: Long, onlyActive:Boolean = false): List<Contract>
    suspend fun getByClientId(clientId: Long, fromDate:LocalDate, toDate: LocalDate): List<Contract>
    suspend fun getCurrentByProductId(productId: Long): Contract?
    suspend fun create(contract: ContractDto): Contract
    suspend fun update(id: Long, contract: ContractDto): Contract
    suspend fun delete(id: Long): Boolean

}