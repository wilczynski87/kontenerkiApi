package com.kontenery.p24.client

import com.kontenery.P24Config
import com.kontenery.p24.dto.P24ApiResponse
import com.kontenery.p24.dto.P24RegisterTransactionRequest
import com.kontenery.p24.dto.P24VerifyApiResponse
import com.kontenery.p24.dto.P24VerifyTransactionRequest
import com.kontenery.p24.exception.P24Exception
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class P24ApiClient(
    private val config: P24Config,
    private val httpClient: HttpClient = HttpClient(OkHttp),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val baseUrl: String = config.baseUrl.trimEnd('/') + "/"

    suspend fun registerTransaction(request: P24RegisterTransactionRequest): String {
        val response = httpClient.post("${baseUrl}transaction/register") {
            header(HttpHeaders.Authorization, basicAuthHeader())
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
        val body = runCatching { response.bodyAsText() }.getOrDefault("")
        if (!response.status.isSuccess()) {
            throw P24Exception(
                "P24 register failed: ${response.status} - $body",
                statusCode = response.status.value,
            )
        }
        val parsed = runCatching { json.decodeFromString<P24ApiResponse>(body) }
            .getOrElse {
                throw P24Exception("P24 register: invalid JSON response: $body", cause = it)
            }
        if (parsed.responseCode != 0) {
            throw P24Exception(
                "P24 register error code=${parsed.responseCode}: ${parsed.error ?: body}",
                statusCode = 502,
            )
        }
        return parsed.data?.token
            ?: throw P24Exception("P24 register: missing token in response: $body", statusCode = 502)
    }

    suspend fun verifyTransaction(request: P24VerifyTransactionRequest) {
        val response = httpClient.post("${baseUrl}transaction/verify") {
            header(HttpHeaders.Authorization, basicAuthHeader())
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
        val body = runCatching { response.bodyAsText() }.getOrDefault("")
        if (!response.status.isSuccess()) {
            throw P24Exception(
                "P24 verify failed: ${response.status} - $body",
                statusCode = response.status.value,
            )
        }
        val parsed = runCatching { json.decodeFromString<P24VerifyApiResponse>(body) }
            .getOrElse {
                throw P24Exception("P24 verify: invalid JSON response: $body", cause = it)
            }
        if (parsed.responseCode != 0) {
            throw P24Exception(
                "P24 verify error code=${parsed.responseCode}: ${parsed.error ?: body}",
                statusCode = 502,
            )
        }
    }

    fun redirectUrl(token: String): String =
        "${config.paymentUrl.trimEnd('/')}/$token"

    private fun basicAuthHeader(): String {
        val posId = config.posId
            ?: throw P24Exception("P24_POS_ID / P24_MERCHANT_ID is not configured", statusCode = 500)
        val apiKey = config.apiKey
            ?: throw P24Exception("P24_API_KEY is not configured", statusCode = 500)
        val encoded = Base64.getEncoder().encodeToString("$posId:$apiKey".toByteArray(Charsets.UTF_8))
        return "Basic $encoded"
    }
}
