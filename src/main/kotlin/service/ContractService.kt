package com.kontenery.service

import com.kontenery.model.Contract

interface ContractService {

    suspend fun getAll(page:Int, size:Int): List<Contract>
    suspend fun getById(id: Long): Contract?
    suspend fun create(contract: Contract): Contract
    suspend fun update(id: Long, contract: Contract): Contract
    suspend fun delete(id: Long): Boolean

}