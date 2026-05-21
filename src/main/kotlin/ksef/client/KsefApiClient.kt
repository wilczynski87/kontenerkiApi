package com.kontenery.ksef.client

import com.kontenery.KsefConfig
import com.kontenery.ksef.dto.KsefAuthChallengeResponse
import com.kontenery.ksef.dto.KsefAuthKsefTokenRequest
import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefInvoiceQueryFilters
import com.kontenery.ksef.dto.KsefOpenOnlineSessionRequest
import com.kontenery.ksef.dto.KsefOpenOnlineSessionResponse
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSendInvoiceOnlineRequest
import com.kontenery.ksef.dto.KsefSendInvoiceOnlineResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefSessionStatusResponse
import com.kontenery.ksef.dto.KsefSignatureResponse
import com.kontenery.ksef.exception.KsefException
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KsefApiClient(
    private val config: KsefConfig,
    private val httpClient: HttpClient = HttpClient(OkHttp),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val baseUrl: String =
        "${config.baseUrl.trimEnd('/')}/${config.apiSuffix.trim('/')}/"

    suspend fun getAuthChallenge(): KsefAuthChallengeResponse =
        postEmpty("auth/challenge")

    suspend fun getPublicKeyCertificates(): List<KsefPublicKeyCertificate> =
        get("security/public-key-certificates")

    suspend fun authenticateByKsefToken(request: KsefAuthKsefTokenRequest): KsefSignatureResponse =
        postJson("auth/ksef-token", request, expectedStatus = HttpStatusCode.Accepted)

    suspend fun getAuthStatus(referenceNumber: String, authenticationToken: String): KsefAuthStatusResponse =
        get("auth/$referenceNumber", authenticationToken)

    suspend fun redeemToken(authenticationToken: String): KsefAuthOperationStatusResponse =
        postEmpty("auth/token/redeem", authenticationToken)

    suspend fun queryInvoiceMetadata(
        accessToken: String,
        pageOffset: Int,
        pageSize: Int,
        sortOrder: String,
        filters: KsefInvoiceQueryFilters,
    ): KsefQueryInvoiceMetadataResponse {
        val response = httpClient.post("${baseUrl}invoices/query/metadata") {
            parameter("pageOffset", pageOffset)
            parameter("pageSize", pageSize)
            parameter("sortOrder", sortOrder)
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(json.encodeToString(filters))
        }
        return parseResponse(response)
    }

    suspend fun openOnlineSession(
        request: KsefOpenOnlineSessionRequest,
        accessToken: String,
    ): KsefOpenOnlineSessionResponse =
        postJson("sessions/online", request, accessToken, HttpStatusCode.Created)

    suspend fun sendInvoiceToOnlineSession(
        sessionReferenceNumber: String,
        request: KsefSendInvoiceOnlineRequest,
        accessToken: String,
    ): KsefSendInvoiceOnlineResponse {
        val response = httpClient.post("${baseUrl}sessions/online/$sessionReferenceNumber/invoices") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
        return parseResponse(response, HttpStatusCode.Accepted)
    }

    suspend fun closeOnlineSession(sessionReferenceNumber: String, accessToken: String) {
        val response = httpClient.post("${baseUrl}sessions/online/$sessionReferenceNumber/close") {
            header("Authorization", "Bearer $accessToken")
            accept(ContentType.Application.Json)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            throw KsefException(
                "KSeF API error: ${response.status} - $body",
                statusCode = response.status.value,
            )
        }
    }

    suspend fun getSessionStatus(sessionReferenceNumber: String, accessToken: String): KsefSessionStatusResponse =
        get("sessions/$sessionReferenceNumber", accessToken)

    suspend fun getSessionInvoiceStatus(
        sessionReferenceNumber: String,
        invoiceReferenceNumber: String,
        accessToken: String,
    ): KsefSessionInvoiceStatusResponse =
        get("sessions/$sessionReferenceNumber/invoices/$invoiceReferenceNumber", accessToken)

    private suspend inline fun <reified T> get(path: String, bearerToken: String? = null): T {
        val response = httpClient.get(baseUrl + path) {
            accept(ContentType.Application.Json)
            bearerToken?.let { header("Authorization", "Bearer $it") }
        }
        return parseResponse(response)
    }

    private suspend inline fun <reified TBody, reified T> postJson(
        path: String,
        body: TBody,
        bearerToken: String? = null,
        expectedStatus: HttpStatusCode = HttpStatusCode.OK,
    ): T {
        val response = httpClient.post(baseUrl + path) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            bearerToken?.let { header("Authorization", "Bearer $it") }
            setBody(json.encodeToString(body))
        }
        return parseResponse(response, expectedStatus)
    }

    private suspend inline fun <reified T> postEmpty(path: String, bearerToken: String? = null): T {
        val response = httpClient.post(baseUrl + path) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            bearerToken?.let { header("Authorization", "Bearer $it") }
        }
        return parseResponse(response)
    }

    private suspend inline fun <reified T> parseResponse(
        response: HttpResponse,
        expectedStatus: HttpStatusCode = response.status,
    ): T {
        if (response.status != expectedStatus && !response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            throw KsefException(
                "KSeF API error: ${response.status} - $body",
                statusCode = response.status.value,
            )
        }
        if (response.status == HttpStatusCode.NoContent) {
            throw KsefException("KSeF API returned empty response for ${response.call.request.url}")
        }
        val text = response.bodyAsText()
        return json.decodeFromString(text)
    }
}
