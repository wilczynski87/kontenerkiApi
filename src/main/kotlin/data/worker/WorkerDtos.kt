package com.kontenery.data.worker

import com.kontenery.data.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Worker(
    val id: Long? = null,
    val clientId: Long? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate? = null,
) {
    fun toDto(): WorkerDto = WorkerDto(
        id = requireNotNull(id) { "Worker ID must not be null" },
        name = name,
        email = email,
        createdAt = createdAt,
    )

    fun resolvePassword(): String? = password?.trim()?.takeUnless { it.isBlank() }
}

@Serializable
data class WorkerDto(
    val id: Long,
    val name: String,
    val email: String,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate? = null,
)

@Serializable
data class WorkerCreateRequest(
    val name: String,
    val email: String,
    val password: String,
)
