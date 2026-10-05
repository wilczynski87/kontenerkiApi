package com.kontenery.repository.entity

import org.jetbrains.exposed.sql.Table

object AppSettingsTable : Table("app_settings") {
    val key = varchar("key", 64)
    val value = varchar("value", 256)

    override val primaryKey = PrimaryKey(key)
}
