package com.kontenery


import com.kontenery.data.utils.Env
import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

private val applicationTables: Array<Table> = arrayOf(
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

private fun resolveDbEnv(apiConfig: ApiConfig): Env = when (apiConfig.env.uppercase()) {
    "PROD", "PRODUCTION" -> Env.PROD
    else -> Env.DEV
}

/** DEV domyślnie uruchamia migrację Exposed; po imporcie zrzutu można wyłączyć: DB_AUTO_MIGRATE=false */
private fun shouldAutoMigrate(apiConfig: ApiConfig): Boolean {
    val override = System.getenv("DB_AUTO_MIGRATE")?.trim()?.lowercase()
    return when (override) {
        "false", "0", "no" -> false
        "true", "1", "yes" -> true
        null, "" -> resolveDbEnv(apiConfig) == Env.DEV
        else -> resolveDbEnv(apiConfig) == Env.DEV
    }
}

fun configureDatabases(apiConfig: ApiConfig) {
    val dbHost = apiConfig.db.host
    val dbPort = apiConfig.db.port
    val dbName = apiConfig.db.name
    val dbUser = apiConfig.db.user
    val dbPassword = apiConfig.db.password
    val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    println("Connecting to: $url")

    val connection = {
        DriverManager.getConnection(url, dbUser, dbPassword)
    }
    val database: Database = Database.connect(connection)

    if (!shouldAutoMigrate(apiConfig)) {
        println("DB_AUTO_MIGRATE disabled — skipping SchemaUtils.createMissingTablesAndColumns")
        return
    }

    transaction(database) {
        try {
            SchemaUtils.createMissingTablesAndColumns(tables = applicationTables)
        } catch (e: Exception) {
            val tableNames = applicationTables.joinToString(", ") { it.tableName }
            throw IllegalStateException(
                "Database schema migration failed (createMissingTablesAndColumns). " +
                    "If you imported a SQL dump, restore into an empty database (see scripts/restore-database.sh) " +
                    "or set DB_AUTO_MIGRATE=false and apply scripts/post-restore-migrations.sql manually. " +
                    "Expected Exposed tables: $tableNames. Cause: ${e.message}",
                e,
            )
        }
    }
}
