package com.kontenery.service.impl

import com.kontenery.GateConfig
import com.kontenery.data.gate.OpenGateResponse
import com.kontenery.data.utils.now
import com.kontenery.repository.GateEventRepo
import com.kontenery.service.ContractService
import com.kontenery.service.GateAccessDeniedException
import com.kontenery.service.GateService
import com.kontenery.service.ListingService
import com.kontenery.service.SuplaTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds

class GateServiceImpl(
    private val gateConfig: GateConfig,
    private val contractService: ContractService,
    private val listingService: ListingService,
    private val gateEventRepo: GateEventRepo,
    private val suplaTokenProvider: SuplaTokenProvider,
    private val httpClient: HttpClient = HttpClient(),
) : GateService {

    private val json = Json { ignoreUnknownKeys = true }

    override fun checkUserAuthenticated(userId: String?): Long {
        if (userId.isNullOrBlank()) {
            throw GateAccessDeniedException("Użytkownik nie jest zalogowany")
        }
        val clientId = userId.toLongOrNull()
            ?: throw GateAccessDeniedException("Błąd userId, nieprawidłowy format")
        return clientId
    }

    override suspend fun ensureActiveContract(clientId: Long) {
        val active = contractService.getByClientId(clientId, onlyActive = true)
        if (active.isEmpty()) {
            throw GateAccessDeniedException("Brak aktywnej rezerwacji na dziś")
        }
    }

    // TODO do poprawy... logika zepsuta
    override suspend fun ensureNoOverdue(clientId: Long) {
        val balance = listingService.clientOverdue(
            clientId,
            LocalDate.now().minus(1, DateTimeUnit.YEAR),
            LocalDate.now(),
        ) ?: BigDecimal.ZERO

        if (balance < BigDecimal.ZERO) {
            throw GateAccessDeniedException("Wykryto nieuregulowane zadłużenie")
        }
    }

    override suspend fun ensureCooldown(clientId: Long) {
        val lastOpenEpochMs = gateEventRepo.getLastOpenEventEpochMs(clientId) ?: return
        val elapsedMs = Clock.System.now().toEpochMilliseconds() - lastOpenEpochMs
        val cooldownMs = gateConfig.cooldownSeconds.seconds.inWholeMilliseconds

        if (elapsedMs < cooldownMs) {
            val remaining = ((cooldownMs - elapsedMs + 999) / 1000).coerceAtLeast(1)
            throw GateAccessDeniedException("Poczekaj $remaining s przed kolejnym otwarciem bramy")
        }
    }

    override suspend fun openGate(): OpenGateResponse {
        triggerGate()
        return OpenGateResponse(
            success = true,
            message = "Brama została otwarta",
        )
    }

    override suspend fun logOpenEvent(clientId: Long) {
        gateEventRepo.logOpenEvent(clientId = clientId, note = "yard")
    }

    private suspend fun triggerGate() {
        if (gateConfig.mockMode) {
            return
        }

        require(gateConfig.openUrl.isNotBlank()) { "GATE_OPEN_URL is missing: ${gateConfig.openUrl}" }

        val method = when (gateConfig.method.uppercase()) {
            "GET" -> HttpMethod.Get
            "POST" -> HttpMethod.Post
            "PUT" -> HttpMethod.Put
            "PATCH" -> HttpMethod.Patch
            else -> throw IllegalArgumentException("Nieobsługiwana metoda GATE_OPEN_METHOD: ${gateConfig.method}")
        }

        var token = suplaTokenProvider.getAccessToken()
        var response = executeGateRequest(method, token)

        if (response.status == HttpStatusCode.Unauthorized) {
            token = suplaTokenProvider.getAccessToken(forceRefresh = true)
            response = executeGateRequest(method, token)
        }

        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.bodyAsText() }.getOrDefault("")
            throw IllegalStateException(
                "Gate hardware error ${response.status} from ${gateConfig.openUrl}: $errorBody".trim()
            )
        }
    }

    private suspend fun executeGateRequest(method: HttpMethod, token: String) =
        httpClient.request(gateConfig.openUrl) {
            this.method = method
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            val body = gateConfig.requestBody
            if (!body.isNullOrBlank() && method != HttpMethod.Get) {
                requireValidJsonObject(body)
                setBody(TextContent(body, ContentType.Application.Json))
            }
        }

    private fun requireValidJsonObject(body: String) {
        val parsed = runCatching { json.parseToJsonElement(body) }.getOrElse { error ->
            throw IllegalStateException(
                "GATE_REQUEST_BODY is not valid JSON (got: $body). " +
                    "If deployed via GitHub Actions, ensure .env quotes are not stripped. Cause: ${error.message}",
            )
        }
        if (parsed !is JsonObject) {
            throw IllegalStateException(
                "GATE_REQUEST_BODY must be a JSON object, e.g. {\"action\":\"OPEN_CLOSE\"} (got: $body)",
            )
        }
    }
}
