package com.kontenery

import java.sql.DriverManager

/**
 * Ensures p24_transaction exists on databases that skip full Exposed
 * auto-migration (API_ENV=PROD or DB_AUTO_MIGRATE=false).
 */
internal fun ensureP24SchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_P24_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS p24_transaction (
                    id BIGSERIAL PRIMARY KEY,
                    session_id VARCHAR(100) NOT NULL UNIQUE,
                    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
                    amount_grosze INTEGER NOT NULL,
                    currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
                    description VARCHAR(255),
                    email VARCHAR(255) NOT NULL,
                    invoice_numbers TEXT NOT NULL DEFAULT '[]',
                    url_return VARCHAR(500) NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    token VARCHAR(255),
                    order_id BIGINT,
                    method_id INTEGER,
                    statement VARCHAR(255),
                    payment_id BIGINT REFERENCES payments(id) ON DELETE SET NULL,
                    error_message VARCHAR(500)
                )
                """.trimIndent(),
            )
        }
    }
}
