package com.kontenery.repository

import com.kontenery.model.Contract

interface ContractRepo {

    suspend fun findAll(page:Int, size:Int): List<Contract>
    suspend fun findById(id: Long): Contract?
    suspend fun findByClientId(clientId: Long): List<Contract>
    suspend fun findCurrentByProductId(productId: Long): List<Contract>
    suspend fun create(contract: Contract): Contract
    suspend fun update(id: Long, contract: Contract): Contract
    suspend fun delete(id: Long): Boolean

}