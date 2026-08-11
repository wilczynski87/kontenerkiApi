package com.kontenery

import java.sql.DriverManager

/**
 * Ensures employees table exists on databases that skip full Exposed auto-migration.
 */
internal fun ensureWorkerSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_WORKER_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS employees (
                    id BIGSERIAL PRIMARY KEY,
                    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
                    name VARCHAR(255) NOT NULL DEFAULT '',
                    email VARCHAR(255) NOT NULL,
                    password TEXT NOT NULL DEFAULT '',
                    created_at DATE,
                    UNIQUE (client_id, email)
                )
                """.trimIndent(),
            )
            stmt.execute("ALTER TABLE employees ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL DEFAULT ''")
            stmt.execute("ALTER TABLE employees ADD COLUMN IF NOT EXISTS password TEXT NOT NULL DEFAULT ''")
        }
    }
}
