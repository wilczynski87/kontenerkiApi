package com.kontenery


import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

fun configureDatabases() {
    "docker run --name db1 -e POSTGRES_USER=admin_user -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=db1 -p 5434:5432 -d postgres:latest"

    val DB_HOST = System.getenv("DB_HOST")
    val DB_PORT = System.getenv("DB_PORT")
    val DB_NAME = System.getenv("DB_NAME")
    val DB_USER = System.getenv("DB_USER")
    val DB_PASSWORD = System.getenv("DB_PASSWORD")
    val url = "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
    println()
    println("url: $url, user: $DB_USER, password: $DB_PASSWORD")
    println()

    val connection = {
        DriverManager.getConnection(url, DB_USER, DB_PASSWORD)
    }
    val database: Database = Database.connect(connection)

    transaction(database) {
//        arrayOf(
//            AddressTable,
//            ClientPersonalDataTable, ClientCompanyDataTable, ClientTable,
//            ProductTable,
//            ContractTable, DepositTable,
//            Subjects, Positions, InvoiceTable,
//            BillTable, PositionsBill,
//            PaymentTable, PaymentInvoices,
//            ClientBankAccountTable,
//        )
//        Unit
        SchemaUtils.createMissingTablesAndColumns(
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