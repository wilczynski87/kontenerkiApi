package com.kontenery


import com.kontenery.library.utils.Env
import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

fun configureDatabases(apiConfig: ApiConfig) {
    "docker run --name db1 -e POSTGRES_USER=admin_user -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=db1 -p 5434:5432 -d postgres:latest"

    val dbHost = apiConfig.db.host
    val dbPort = apiConfig.db.port
    val dbName = apiConfig.db.name
    val dbUser = apiConfig.db.user
    val dbPassword = apiConfig.db.password
    val env: Env = Env.valueOf(System.getenv("ENV") ?: "DEV")
    val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    println()
    println("url: $url, user: $dbUser, password: $dbPassword")
    println()

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
            SubmeterTable, ReadingTable
        )
    }
}