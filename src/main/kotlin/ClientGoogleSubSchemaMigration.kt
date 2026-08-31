package com.kontenery

import java.sql.DriverManager

/**
 * Ensures clients.google_sub exists on databases that skip full Exposed auto-migration.
 */
internal fun ensureClientGoogleSubSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_CLIENT_GOOGLE_SUB_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                ALTER TABLE clients ADD COLUMN IF NOT EXISTS google_sub TEXT
                """.trimIndent(),
            )
            stmt.execute(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS uq_clients_google_sub
                ON clients (google_sub)
                WHERE google_sub IS NOT NULL
                """.trimIndent(),
            )
        }
    }
}
