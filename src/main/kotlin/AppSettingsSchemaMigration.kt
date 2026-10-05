package com.kontenery

import java.sql.DriverManager

/**
 * Ensures app_settings exists on databases that skip full Exposed
 * auto-migration (API_ENV=PROD or DB_AUTO_MIGRATE=false).
 */
internal fun ensureAppSettingsSchemaIfNeeded(apiConfig: ApiConfig) {
    if (System.getenv("DB_APP_SETTINGS_SCHEMA_MIGRATE")?.trim()?.lowercase() in setOf("false", "0", "no")) {
        return
    }

    val url = "jdbc:postgresql://${apiConfig.db.host}:${apiConfig.db.port}/${apiConfig.db.name}"
    DriverManager.getConnection(url, apiConfig.db.user, apiConfig.db.password).use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS app_settings (
                    key VARCHAR(64) PRIMARY KEY,
                    value VARCHAR(256) NOT NULL
                )
                """.trimIndent(),
            )
        }
    }
}
