package com.kontenery.repository.entity

import org.jetbrains.exposed.sql.Table

/**
 * Pojedynczy wiersz z tokenami OAuth SUPLA (rotowany refresh_token musi przeżyć restart API).
 */
object SuplaTokenTable : Table("supla_token") {
    val id = varchar("id", 50)
    val accessToken = text("access_token").nullable()
    val refreshToken = text("refresh_token").nullable()
    val accessTokenExpiresAtEpochMs = long("access_token_expires_at_epoch_ms").nullable()

    override val primaryKey = PrimaryKey(id)
}
