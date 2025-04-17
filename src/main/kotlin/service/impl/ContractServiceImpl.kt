package com.kontenery.service.impl

import com.kontenery.model.*
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

    override suspend fun getByClientId(clientId: Long): List<Contract> {
        return repo.findByClientId(clientId)
    }


    override suspend fun create(contractDto: ContractDto): Contract {

        val contract: Contract = Contract(
            startDate = contractDto.startDate,
            endDate = contractDto.endDate,
            netPrice = contractDto.netPrice,
            vatRate = contractDto.vatRate,
            needInvoice = contractDto.needInvoice
        )

        run {
            val clientId: Long =
                contractDto.client ?: throw NullPointerException("There is no id of a client, for contract")

            val client: Client = clientService.findClientById(clientId)
                ?: throw NullPointerException("There is no client with given ID $clientId")

            contract.client = client
        }

        run {
            val productId: Long = contractDto.product ?: throw NullPointerException("There is no id of a product, for contract")

            val product = productService.findProductById(productId) ?: throw NullPointerException("There is no product with given ID $productId")

            println("product: ${product}")

            contract.product = product as Product
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