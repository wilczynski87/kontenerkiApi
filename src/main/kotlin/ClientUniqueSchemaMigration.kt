package com.kontenery

import org.slf4j.LoggerFactory
import java.sql.DriverManager

/**
 * Best-effort unique indexes on client emails and PESEL.
 * Skips creation when duplicates already exist (logs a warning).
 * Disable with DB_CLIENT_UNIQUE_MIGRATE=false.
 */
internal fun ensureClientUniqueIndexesIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_CLIENT_UNIQUE_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val log = LoggerFactory.getLogger("ClientUniqueSchemaMigration")
    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            tryCreatePartialUniqueIndex(
                stmt = stmt,
                log = log,
                indexName = "client_personal_data_email_unique",
                createSql = """
                    CREATE UNIQUE INDEX IF NOT EXISTS client_personal_data_email_unique
                    ON client_personal_data (lower(btrim(email)))
                    WHERE email IS NOT NULL AND btrim(email) <> ''
                """.trimIndent(),
                duplicateCheckSql = """
                    SELECT COUNT(*) FROM (
                        SELECT lower(btrim(email)) FROM client_personal_data
                        WHERE email IS NOT NULL AND btrim(email) <> ''
                        GROUP BY lower(btrim(email)) HAVING COUNT(*) > 1
                    ) dups
                """.trimIndent(),
            )
            tryCreatePartialUniqueIndex(
                stmt = stmt,
                log = log,
                indexName = "client_company_data_email_unique",
                createSql = """
                    CREATE UNIQUE INDEX IF NOT EXISTS client_company_data_email_unique
                    ON client_company_data (lower(btrim(email)))
                    WHERE email IS NOT NULL AND btrim(email) <> ''
                """.trimIndent(),
                duplicateCheckSql = """
                    SELECT COUNT(*) FROM (
                        SELECT lower(btrim(email)) FROM client_company_data
                        WHERE email IS NOT NULL AND btrim(email) <> ''
                        GROUP BY lower(btrim(email)) HAVING COUNT(*) > 1
                    ) dups
                """.trimIndent(),
            )
            tryCreatePartialUniqueIndex(
                stmt = stmt,
                log = log,
                indexName = "client_personal_data_pesel_unique",
                createSql = """
                    CREATE UNIQUE INDEX IF NOT EXISTS client_personal_data_pesel_unique
                    ON client_personal_data (btrim(pesel))
                    WHERE pesel IS NOT NULL AND btrim(pesel) <> ''
                """.trimIndent(),
                duplicateCheckSql = """
                    SELECT COUNT(*) FROM (
                        SELECT btrim(pesel) FROM client_personal_data
                        WHERE pesel IS NOT NULL AND btrim(pesel) <> ''
                        GROUP BY btrim(pesel) HAVING COUNT(*) > 1
                    ) dups
                """.trimIndent(),
            )
        }
    }
}

private fun tryCreatePartialUniqueIndex(
    stmt: java.sql.Statement,
    log: org.slf4j.Logger,
    indexName: String,
    createSql: String,
    duplicateCheckSql: String,
) {
    try {
        stmt.executeQuery(duplicateCheckSql).use { rs ->
            rs.next()
            val dupCount = rs.getLong(1)
            if (dupCount > 0) {
                log.warn(
                    "Skipping unique index {} — {} duplicate value(s) exist. " +
                        "Clean duplicates, then restart or create the index manually.",
                    indexName,
                    dupCount,
                )
                return
            }
        }
        stmt.execute(createSql)
        log.info("Ensured unique index {}", indexName)
    } catch (e: Exception) {
        log.warn("Could not ensure unique index {}: {}", indexName, e.message)
    }
}
