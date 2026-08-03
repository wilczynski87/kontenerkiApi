package com.kontenery.p24.dto

import kotlinx.serialization.Serializable

@Serializable
enum class P24TransactionStatus {
    PENDING,
    REGISTERED,
    NOTIFIED,
    VERIFIED,
    FAILED,
}

@Serializable
data class P24CreateTransactionRequest(
    val clientId: Long,
    /** Kwota w PLN (np. 12.34). */
    val amount: Double,
    val email: String? = null,
    val description: String? = null,
    val invoiceNumbers: List<String> = emptyList(),
    /** URL powrotu klienta po płatności (wymagany przez P24). */
    val urlReturn: String,
    val currency: String = "PLN",
    val language: String = "pl",
    val country: String = "PL",
)

/** Body for JWT Magazynki clients — clientId is taken from the access token. */
@Serializable
data class P24CreateForClientRequest(
    /** Kwota w PLN (np. 12.34). */
    val amount: Double,
    val email: String? = null,
    val description: String? = null,
    val invoiceNumbers: List<String> = emptyList(),
    /** URL powrotu po płatności; gdy brak — używany jest domyślny. */
    val urlReturn: String? = null,
    val currency: String = "PLN",
    val language: String = "pl",
    val country: String = "PL",
)

@Serializable
data class P24CreateTransactionResponse(
    val sessionId: String,
    val token: String,
    val redirectUrl: String,
    val amountGrosze: Int,
    val currency: String,
    val status: P24TransactionStatus,
    val mock: Boolean = false,
)

@Serializable
data class P24TransactionStatusResponse(
    val sessionId: String,
    val status: P24TransactionStatus,
    val orderId: Long? = null,
    val amountGrosze: Int,
    val currency: String,
    val paymentId: Long? = null,
    val invoiceNumbers: List<String> = emptyList(),
)

/** Notification payload from P24 (urlStatus webhook). */
@Serializable
data class P24NotificationPayload(
    val merchantId: Int,
    val posId: Int,
    val sessionId: String,
    val amount: Int,
    val originAmount: Int,
    val currency: String,
    val orderId: Long,
    val methodId: Int,
    val statement: String = "",
    val sign: String,
)

@Serializable
data class P24RegisterTransactionRequest(
    val merchantId: Int,
    val posId: Int,
    val sessionId: String,
    val amount: Int,
    val currency: String,
    val description: String,
    val email: String,
    val country: String = "PL",
    val language: String = "pl",
    val urlReturn: String,
    val urlStatus: String,
    val sign: String,
)

@Serializable
data class P24TokenData(
    val token: String,
)

@Serializable
data class P24ApiResponse(
    val data: P24TokenData? = null,
    val responseCode: Int = 0,
    val error: String? = null,
)

@Serializable
data class P24VerifyTransactionRequest(
    val merchantId: Int,
    val posId: Int,
    val sessionId: String,
    val amount: Int,
    val currency: String,
    val orderId: Long,
    val sign: String,
)

@Serializable
data class P24VerifyData(
    val status: String? = null,
)

@Serializable
data class P24VerifyApiResponse(
    val data: P24VerifyData? = null,
    val responseCode: Int = 0,
    val error: String? = null,
)
