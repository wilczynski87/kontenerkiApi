package com.kontenery


import com.kontenery.data.utils.Env
import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.DriverManager

private val dbLog = LoggerFactory.getLogger("Databases")

fun configureDatabases(apiConfig: ApiConfig) {
    val dbHost = apiConfig.db.host
    val dbPort = apiConfig.db.port
    val dbName = apiConfig.db.name
    val dbUser = apiConfig.db.user
    val dbPassword = apiConfig.db.password
    val env: Env = Env.valueOf(System.getenv("ENV") ?: "DEV")
    val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    dbLog.info("Connecting to PostgreSQL at {}:{}/{}", dbHost, dbPort, dbName)

    val connection = {
        DriverManager.getConnection(url, dbUser, dbPassword)
    }
    val database: Database = Database.connect(connection)

    transaction(database) {
        if(env == Env.PROD) {
            arrayOf(
                AddressTable,
                ClientPersonalDataTable, ClientCompanyDataTable, ClientTable,
                ProductTable,
                ContractTable, DepositTable,
                Subjects, Positions, InvoiceTable,
                BillTable, PositionsBill,
                PaymentTable, PaymentInvoices,
                ClientBankAccountTable,
                SubmeterTable, ReadingTable,
                KsefSessionInvoiceStatusTable,
            )
            Unit
        } else SchemaUtils.createMissingTablesAndColumns(
            AddressTable,
            ClientPersonalDataTable, ClientCompanyDataTable, ClientTable,
            ProductTable,
            ContractTable, DepositTable,
            Subjects, Positions, InvoiceTable,
            BillTable, PositionsBill,
            PaymentTable, PaymentInvoices,
            ClientBankAccountTable,
            SubmeterTable, ReadingTable,
            KsefSessionInvoiceStatusTable,
        )
    }
}