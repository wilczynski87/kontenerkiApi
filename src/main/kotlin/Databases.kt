package com.kontenery

import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.DriverManager

private val dbLog = LoggerFactory.getLogger("Databases")

private val schemaTables = arrayOf(
    AddressTable,
    ClientPersonalDataTable,
    ClientCompanyDataTable,
    ClientTable,
    ProductTable,
    ContractTable,
    DepositTable,
    Subjects,
    Positions,
    InvoiceTable,
    BillTable,
    PositionsBill,
    PaymentTable,
    PaymentInvoices,
    ClientBankAccountTable,
    SubmeterTable,
    ReadingTable,
    KsefSessionInvoiceStatusTable,
)

fun configureDatabases(apiConfig: ApiConfig) {
    val dbHost = apiConfig.db.host
    val dbPort = apiConfig.db.port
    val dbName = apiConfig.db.name
    val dbUser = apiConfig.db.user
    val dbPassword = apiConfig.db.password
    val isProd = apiConfig.env.equals("PROD", ignoreCase = true)
    val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    dbLog.info("Connecting to PostgreSQL at {}:{}/{} (API_ENV={})", dbHost, dbPort, dbName, apiConfig.env)

    val connection = {
        DriverManager.getConnection(url, dbUser, dbPassword)
    }
    val database: Database = Database.connect(connection)

    transaction(database) {
        if (isProd) {
            dbLog.info("API_ENV=PROD — automatic schema sync disabled; apply SQL migrations manually")
        } else {
            dbLog.info("API_ENV={} — running SchemaUtils.createMissingTablesAndColumns", apiConfig.env)
            SchemaUtils.createMissingTablesAndColumns(*schemaTables)
            dbLog.info("Schema sync finished")
        }
    }
}