package com.kontenery

import java.sql.DriverManager

/**
 * Ensures gate_event table exists on databases that skip full Exposed
 * auto-migration (API_ENV=PROD or DB_AUTO_MIGRATE=false).
 */
internal fun ensureGateEventSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_GATE_EVENT_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS gate_event (
                    id BIGSERIAL PRIMARY KEY,
                    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
                    opened_at_epoch_ms BIGINT NOT NULL,
                    note VARCHAR(100)
                )
                """.trimIndent(),
            )
        }
    }
}
