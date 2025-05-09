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

    override suspend fun getCurrentByProductId(productId: Long): Contract? {
        val contracts: List<Contract> = repo.findCurrentByProductId(productId)
        return when(contracts.size) {
            0 -> null
            1 -> contracts.first()
            else -> throw IllegalArgumentException("Too many Contracts, for given product id: $productId")
        }
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
            val productId: Long =
                contractDto.product ?: throw NullPointerException("There is no id of a product, for contract")

            var product = productService.findProductById(productId) as Product ?: throw NullPointerException("There is no product with given ID $productId")

            println("product: ${product}")

            // updating Client for Product
            product.client = contract.client
            product = productService.updateProduct(product) as Product

            println("product2: ${product}")

            contract.product = product
        }

        return repo.create(contract)
    }

    override suspend fun update(contractId: Long, contractDto: ContractDto): Contract {
        var contract:Contract?

        run {
            contract = getById(contractId) ?: throw NullPointerException("There is no Contract with given id $contractId")

            contract?.apply {
                startDate = contractDto.startDate
                endDate = contractDto.endDate
                netPrice = contractDto.netPrice
                needInvoice = contractDto.needInvoice
            }
        }

        run {
            val clientId: Long =
                contractDto.client ?: throw NullPointerException("There is no id of a client, for contract")

            val clientFound: Client = clientService.findClientById(clientId)
                ?: throw NullPointerException("There is no client with given ID $clientId")

            contract?.client = clientFound
        }

        run {
            val productId: Long =
                contractDto.product ?: throw NullPointerException("There is no id of a product, for contract")

            var product = productService.findProductById(productId) as Product ?: throw NullPointerException("There is no product with given ID $productId")

            println("product: ${product}")

            // updating Client for Product
            product.client = contract?.client
            product = productService.updateProduct(product) as Product

            println("product2: ${product}")

            contract?.product = product
        }

        return repo.update(contractId, contract!!)
    }

    override suspend fun delete(id: Long): Boolean {
        return repo.delete(id)
    }

}