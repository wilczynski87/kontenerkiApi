package com.kontenery

import java.sql.DriverManager

/**
 * Ensures clients.password exists on databases that skip full Exposed
 * auto-migration (API_ENV=PROD or DB_AUTO_MIGRATE=false).
 */
internal fun ensureClientPasswordSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_CLIENT_PASSWORD_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                ALTER TABLE clients ADD COLUMN IF NOT EXISTS password TEXT
                """.trimIndent(),
            )
        }
    }
}
