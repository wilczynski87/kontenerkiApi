package com.kontenery.repository.impl

import com.kontenery.repository.AppSettingsRepo
import com.kontenery.repository.entity.AppSettingsTable
import com.kontenery.repository.entity.suspendTransaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class AppSettingsRepoImpl : AppSettingsRepo {

    override suspend fun get(key: String): String? = suspendTransaction {
        AppSettingsTable
            .selectAll()
            .where { AppSettingsTable.key eq key }
            .singleOrNull()
            ?.get(AppSettingsTable.value)
    }

    override suspend fun set(key: String, value: String): Unit = suspendTransaction {
        val updated = AppSettingsTable.update({ AppSettingsTable.key eq key }) {
            it[AppSettingsTable.value] = value
        }
        if (updated == 0) {
            AppSettingsTable.insert {
                it[AppSettingsTable.key] = key
                it[AppSettingsTable.value] = value
            }
        }
    }
}
