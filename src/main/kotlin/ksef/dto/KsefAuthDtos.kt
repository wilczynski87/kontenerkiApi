package com.kontenery.ksef.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KsefAuthChallengeResponse(
    val challenge: String,
    val timestamp: String? = null,
    @SerialName("timestampMs")
    val timestampMs: Long? = null,
    val clientIp: String? = null,
)

@Serializable
data class KsefContextIdentifier(
    val type: String,
    val value: String,
)

@Serializable
data class KsefAuthKsefTokenRequest(
    val challenge: String,
    val contextIdentifier: KsefContextIdentifier,
    val encryptedToken: String,
    val publicKeyId: String? = null,
)

@Serializable
data class KsefTokenInfo(
    val token: String,
    val validUntil: String? = null,
)

@Serializable
data class KsefSignatureResponse(
    val referenceNumber: String,
    val authenticationToken: KsefTokenInfo,
)

@Serializable
data class KsefStatusInfo(
    val code: Int,
    val description: String? = null,
)

@Serializable
data class KsefAuthStatusResponse(
    val status: KsefStatusInfo,
)

@Serializable
data class KsefAuthOperationStatusResponse(
    val accessToken: KsefTokenInfo,
    val refreshToken: KsefTokenInfo? = null,
)

@Serializable
data class KsefPublicKeyCertificate(
    val certificate: String,
    val publicKeyId: String? = null,
    val certificateId: String? = null,
    val validFrom: String? = null,
    val validTo: String? = null,
    val usage: List<String> = emptyList(),
)

@Serializable
data class KsefLoginResponse(
    val authenticated: Boolean = true,
    val validUntil: String? = null,
)
