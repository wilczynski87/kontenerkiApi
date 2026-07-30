package com.kontenery.repository.impl

import com.kontenery.repository.SuplaStoredTokens
import com.kontenery.repository.SuplaTokenRepo
import com.kontenery.repository.entity.SuplaTokenTable
import com.kontenery.repository.entity.suspendTransaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class SuplaTokenRepoImpl : SuplaTokenRepo {

    override suspend fun load(): SuplaStoredTokens? = suspendTransaction {
        SuplaTokenTable
            .selectAll()
            .where { SuplaTokenTable.id eq DOCUMENT_ID }
            .singleOrNull()
            ?.let { row ->
                SuplaStoredTokens(
                    accessToken = row[SuplaTokenTable.accessToken],
                    refreshToken = row[SuplaTokenTable.refreshToken],
                    accessTokenExpiresAtEpochMs = row[SuplaTokenTable.accessTokenExpiresAtEpochMs],
                )
            }
    }

    override suspend fun save(tokens: SuplaStoredTokens): Unit = suspendTransaction {
        val updated = SuplaTokenTable.update({ SuplaTokenTable.id eq DOCUMENT_ID }) {
            it[accessToken] = tokens.accessToken
            it[refreshToken] = tokens.refreshToken
            it[accessTokenExpiresAtEpochMs] = tokens.accessTokenExpiresAtEpochMs
        }
        if (updated == 0) {
            SuplaTokenTable.insert {
                it[id] = DOCUMENT_ID
                it[accessToken] = tokens.accessToken
                it[refreshToken] = tokens.refreshToken
                it[accessTokenExpiresAtEpochMs] = tokens.accessTokenExpiresAtEpochMs
            }
        }
    }

    companion object {
        const val DOCUMENT_ID = "supla_oauth"
    }
}
