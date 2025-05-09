package com.kontenery.service

import com.kontenery.model.Contract
import com.kontenery.model.ContractDto

interface ContractService {

    suspend fun getAll(page:Int, size:Int): List<Contract>
    suspend fun getById(id: Long): Contract?
    suspend fun getByClientId(clientId: Long): List<Contract>
    suspend fun getCurrentByProductId(productId: Long): Contract?
    suspend fun create(contract: ContractDto): Contract
    suspend fun update(id: Long, contract: ContractDto): Contract
    suspend fun delete(id: Long): Boolean

}