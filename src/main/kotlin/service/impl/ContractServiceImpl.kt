package com.kontenery.service.impl

import com.kontenery.model.Client
import com.kontenery.model.Container
import com.kontenery.model.Contract
import com.kontenery.model.Yard
import com.kontenery.repository.ContractRepo
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.ProductService

class ContractServiceImpl(
    private val repo: ContractRepo,
    private val clientService: ClientService,
    private val productService: ProductService,
): ContractService {

    override suspend fun getAll(page:Int, size:Int): List<Contract> {
        return repo.findAll(page, size)
    }

    override suspend fun getById(id: Long): Contract? {
        return repo.findById(id)
    }

    override suspend fun create(contract: Contract): Contract {

        run {
            val clientId: Long =
                contract.client?.id ?: throw NullPointerException("There is no id of a client, for contract")

            val client: Client = clientService.findClientById(clientId)
                ?: throw NullPointerException("There is no client with given ID $clientId")

            contract.apply { this.client = client }
        }

        run {
            val productId: Long = contract.product?.id ?: throw NullPointerException("There is no id of a product, for contract")

            val product = productService.findProductById(productId) ?: throw NullPointerException("There is no product with given ID $productId")

            println("product: ${product}")

            contract.product = product
        }

        return repo.create(contract)
    }

    override suspend fun update(id: Long, contract: Contract): Contract {
        return repo.update(id, contract)
    }

    override suspend fun delete(id: Long): Boolean {
        return repo.delete(id)
    }

}