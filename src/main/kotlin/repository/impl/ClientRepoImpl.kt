package com.kontenery.repository.impl
import com.kontenery.model.Address
import com.kontenery.model.Client
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientPersonalData
import com.kontenery.repository.AddressRepo
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.AddressTable.postCode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction

class ClientRepoImpl(val addressRepo: AddressRepo): ClientRepo {

    override suspend fun save(client: Client): Client? {
        val clientPersonalData: ClientPersonalData? = client.clientPrivate
        val clientCompanyData: ClientCompanyData? = client.clientCompany
        val isActive: Boolean? = client.isActive

        return suspendTransaction {

            val personalDataEntity = clientPersonalData?.let { personalData ->
                ClientPersonalDataEntity.new {
                    firstName = personalData.firstName
                    lastName = personalData.lastName
                    pesel = personalData.pesel
                    passport = personalData.passport
                    phone = personalData.phone
                    email = personalData.email

                    personalData.address?.let { address ->
                        this.address = AddressDAO.new {
                            street = address.street
                            city = address.city
                            postcode = address.postCode
                            country = address.country
                        }
                    }
                }
            }

            val companyDataEntity = clientCompanyData?.let { companyData ->
                ClientCompanyDataEntity.new {
                    name = companyData.name
                    nip = companyData.nip
                    krs = companyData.krs
                    phone = companyData.phone
                    email = companyData.email
                    needInvoice = companyData.needInvoice

                    companyData.address?.let { address ->
                        this.address = AddressDAO.new {
                            street = address.street
                            city = address.city
                            postcode = address.postCode
                            country = address.country
                        }
                    }
                }
            }

            ClientEntity.new {
                this.personalData = personalDataEntity
                this.companyData = companyDataEntity
                this.isActive = isActive
            }.toClient()
        }
    }

    fun updateClient(id: Long, update: Client.() -> Unit): Client? = transaction {
        ClientEntity.findById(id)?.apply {

            val client = toClient().apply(update)

            isActive = client.isActive ?: isActive

            // Update personal data if exists
            client.clientPrivate?.let { personalData ->
                this.personalData?.apply {
                    firstName = personalData.firstName ?: firstName
                    lastName = personalData.lastName ?: lastName
                    pesel = personalData.pesel ?: pesel
                    passport = personalData.passport ?: passport
                    phone = personalData.phone ?: phone
                    email = personalData.email ?: email

                    personalData.address?.let { address ->
                        this.address?.apply {
                            street = address.street ?: street
                            city = address.city ?: city
                            postcode = (address.postCode ?: postCode).toString()
                            country = address.country ?: country
                        } ?: run {
                            this.address = AddressDAO.new {
                                street = address.street ?: ""
                                city = address.city ?: ""
                                postcode = address.postCode ?: ""
                                country = address.country ?: ""
                            }
                        }
                    }
                } ?: run {
                    this.personalData = ClientPersonalDataEntity.new {
                        firstName = personalData.firstName
                        lastName = personalData.lastName
                        pesel = personalData.pesel
                        passport = personalData.passport
                        phone = personalData.phone
                        email = personalData.email

                        personalData.address?.let { address ->
                            this.address = AddressDAO.new {
                                street = address.street ?: ""
                                city = address.city ?: ""
                                postcode = address.postCode ?: ""
                                country = address.country ?: ""
                            }
                        }
                    }
                }
            }

            // Update company data if exists
            client.clientCompany?.let { companyData ->
                this.companyData?.apply {
                    name = companyData.name ?: name
                    nip = companyData.nip ?: nip
                    krs = companyData.krs ?: krs
                    phone = companyData.phone ?: phone
                    email = companyData.email ?: email
                    needInvoice = companyData.needInvoice ?: needInvoice

                    companyData.address?.let { address ->
                        this.address?.apply {
                            street = address.street ?: street
                            city = address.city ?: city
                            postcode = (address.postCode ?: postCode).toString()
                            country = address.country ?: country
                        } ?: run {
                            this.address = AddressDAO.new {
                                street = address.street ?: ""
                                city = address.city ?: ""
                                postcode = address.postCode ?: ""
                                country = address.country ?: ""
                            }
                        }
                    }
                } ?: run {
                    this.companyData = ClientCompanyDataEntity.new {
                        name = companyData.name
                        nip = companyData.nip
                        krs = companyData.krs
                        phone = companyData.phone
                        email = companyData.email
                        needInvoice = companyData.needInvoice

                        companyData.address?.let { address ->
                            this.address = AddressDAO.new {
                                street = address.street ?: ""
                                city = address.city ?: ""
                                postcode = address.postCode ?: ""
                                country = address.country ?: ""
                            }
                        }
                    }
                }
            }

            val instant = Clock.System.now()
            updatedAt = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }?.toClient()
    }

//        fun deleteClient(id: Long): Boolean = transaction {
//            ClientEntity.findById(id)?.delete() != null
//        }

//        fun deactivateClient(id: Long): Client? = transaction {
//            ClientEntity.findById(id)?.apply {
//                isActive = false
//                updatedAt = java.time.LocalDateTime.now()
//            }?.toClient()
//        }


    override suspend fun getAllClients(page: Int, size: Int): List<Client> {
        val countOffset: Long = (page * size).toLong()
        return suspendTransaction {
            ClientEntity.all()
                .limit(size)
                .offset(countOffset)
                .with(ClientEntity::personalData, ClientEntity::companyData)
                .map { it.toClient() }
        }
    }

    override suspend fun findClientById(id: Long): Client? {
        return suspendTransaction {
            ClientEntity.findById(id)?.toClient()
        }
    }

    override suspend fun updateClient(client: Client): Client? {
        requireNotNull(client.id) { "Client ID must not be null for update" }

        return suspendTransaction {
            ClientEntity.findByIdAndUpdate(client.id) { entity ->
                entity.isActive = client.isActive ?: entity.isActive

                client.clientPrivate?.let { personalData ->
                    entity.personalData?.apply {
                        updatePersonalData(personalData)
                    } ?: run {
                        entity.personalData = null
                    }
                }

                client.clientCompany?.let { companyData ->
                    entity.companyData?.apply {
                        updateCompanyData(companyData)
                    } ?: run {
                        entity.companyData = null
                    }
                }
            }?.toClient()
        }
    }

    private fun ClientPersonalDataEntity.updatePersonalData(data: ClientPersonalData) {
        firstName = data.firstName ?: firstName
        lastName = data.lastName ?: lastName
        pesel = data.pesel ?: pesel
        passport = data.passport ?: passport
        phone = data.phone ?: phone
        email = data.email ?: email

        data.address?.let { address ->
            this.address?.updateAddress(address) ?: run {
                this.address = AddressDAO.new {
                    street = address.street ?: ""
                    house = address.house ?: ""
                    city = address.city ?: ""
                    postcode = address.postCode ?: ""
                    country = address.country ?: ""
                }
            }
        }
    }

    private fun ClientCompanyDataEntity.updateCompanyData(data: ClientCompanyData) {
        name = data.name ?: name
        nip = data.nip ?: nip
        krs = data.krs ?: krs
        phone = data.phone ?: phone
        email = data.email ?: email
        needInvoice = data.needInvoice ?: needInvoice

        data.address?.let { address ->
            this.address?.updateAddress(address) ?: run {
                this.address = AddressDAO.new {
                    street = address.street ?: ""
                    house = address.house ?: ""
                    city = address.city ?: ""
                    postcode = address.postCode ?: ""
                    country = address.country ?: ""
                }
            }
        }
    }

    private fun AddressDAO.updateAddress(address: Address) {
        street = address.street ?: street
        house = address.house ?: house
        city = address.city ?: city
        postcode = (address.postCode ?: postCode).toString()
        country = address.country ?: country
    }
}
