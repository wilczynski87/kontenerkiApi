package com.kontenery.repository.entity

import com.kontenery.data.worker.Worker
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object WorkerTable : LongIdTable("employees") {
    val client = reference("client_id", ClientTable, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val password = text("password")
    val createdAt = date("created_at").nullable()

    init {
        index(isUnique = true, columns = arrayOf(client, email))
    }
}

class WorkerEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<WorkerEntity>(WorkerTable)

    var client by ClientEntity referencedOn WorkerTable.client
    var name by WorkerTable.name
    var email by WorkerTable.email
    var password by WorkerTable.password
    var createdAt by WorkerTable.createdAt

    fun toWorker() = Worker(
        id = id.value,
        clientId = client.id.value,
        name = name,
        email = email,
        password = password,
        createdAt = createdAt,
    )
}
