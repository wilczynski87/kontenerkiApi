package com.kontenery

import org.slf4j.LoggerFactory
import java.sql.DriverManager

/**
 * Renames legacy payments.to_account value BUSSINESS (typo) to BUSINESS.
 * Disable with DB_PAYMENT_TO_ACCOUNT_MIGRATE=false.
 */
internal fun ensurePaymentToAccountSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_PAYMENT_TO_ACCOUNT_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val log = LoggerFactory.getLogger("PaymentToAccountSchemaMigration")
    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            val updated = stmt.executeUpdate(
                """
                UPDATE payments
                SET to_account = 'BUSINESS'
                WHERE to_account = 'BUSSINESS'
                """.trimIndent(),
            )
            if (updated > 0) {
                log.info("Renamed legacy to_account BUSSINESS -> BUSINESS on {} payment(s)", updated)
            }
        }
    }
}
