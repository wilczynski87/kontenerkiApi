package com.kontenery

import java.sql.DriverManager

/**
 * Ensures KSeF-related PostgreSQL objects exist on databases that skip full Exposed
 * auto-migration (API_ENV=PROD or DB_AUTO_MIGRATE=false).
 */
internal fun ensureKsefSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_KSEF_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                ALTER TABLE invoice ADD COLUMN IF NOT EXISTS ksef_number VARCHAR(100)
                """.trimIndent(),
            )
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS ksef_session_invoice_status (
                    id BIGSERIAL PRIMARY KEY,
                    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
                    reference_number VARCHAR(100),
                    invoice_number VARCHAR(50),
                    ksef_number VARCHAR(100),
                    status_code INTEGER,
                    status_description VARCHAR(500),
                    permanent_storage_date VARCHAR(50)
                )
                """.trimIndent(),
            )
        }
    }
}
