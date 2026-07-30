package com.kontenery

import java.sql.DriverManager

/**
 * Ensures supla_token table exists on databases that skip full Exposed
 * auto-migration (API_ENV=PROD or DB_AUTO_MIGRATE=false).
 */
internal fun ensureSuplaTokenSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_SUPLA_TOKEN_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS supla_token (
                    id VARCHAR(50) PRIMARY KEY,
                    access_token TEXT,
                    refresh_token TEXT,
                    access_token_expires_at_epoch_ms BIGINT
                )
                """.trimIndent(),
            )
        }
    }
}
