package com.kontenery

import org.slf4j.LoggerFactory
import java.sql.DriverManager

/**
 * Best-effort unique indexes on invoice_number / bill_number.
 * Skips creation when duplicates already exist (logs a warning).
 * Disable with DB_INVOICE_NUMBER_UNIQUE_MIGRATE=false.
 */
internal fun ensureInvoiceNumberUniqueIndexesIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_INVOICE_NUMBER_UNIQUE_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val log = LoggerFactory.getLogger("InvoiceNumberUniqueSchemaMigration")
    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            tryCreateUniqueIndex(
                stmt = stmt,
                log = log,
                table = "invoice",
                column = "invoice_number",
                indexName = "invoice_invoice_number_unique",
                duplicateCheckSql = """
                    SELECT COUNT(*) FROM (
                        SELECT invoice_number FROM invoice
                        GROUP BY invoice_number HAVING COUNT(*) > 1
                    ) dups
                """.trimIndent(),
            )
            tryCreateUniqueIndex(
                stmt = stmt,
                log = log,
                table = "bill",
                column = "bill_number",
                indexName = "bill_bill_number_unique",
                duplicateCheckSql = """
                    SELECT COUNT(*) FROM (
                        SELECT bill_number FROM bill
                        GROUP BY bill_number HAVING COUNT(*) > 1
                    ) dups
                """.trimIndent(),
            )
        }
    }
}

private fun tryCreateUniqueIndex(
    stmt: java.sql.Statement,
    log: org.slf4j.Logger,
    table: String,
    column: String,
    indexName: String,
    duplicateCheckSql: String,
) {
    try {
        stmt.executeQuery(duplicateCheckSql).use { rs ->
            rs.next()
            val dupCount = rs.getLong(1)
            if (dupCount > 0) {
                log.warn(
                    "Skipping unique index on {}.{} — {} duplicate value(s) exist. " +
                        "Clean duplicates, then restart or create index {} manually.",
                    table,
                    column,
                    dupCount,
                    indexName,
                )
                return
            }
        }
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS $indexName ON $table ($column)")
        log.info("Ensured unique index {} on {}.{}", indexName, table, column)
    } catch (e: Exception) {
        log.warn("Could not ensure unique index {} on {}.{}: {}", indexName, table, column, e.message)
    }
}
