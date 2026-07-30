package com.kontenery.service.impl

import com.kontenery.GateConfig
import com.kontenery.repository.SuplaStoredTokens
import com.kontenery.repository.SuplaTokenRepo
import com.kontenery.service.SuplaTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Automatyczne odświeżanie tokenów SUPLA OAuth:
 * 1. access token w cache (~1h), odnawiany przed wygaśnięciem
 * 2. refresh token rotowany przez SUPLA – zapisywany w PostgreSQL (przeżywa restart)
 * 3. przy invalid_grant: reload z DB → live odczyt .env → GATE_ACCESS_TOKEN
 */
class SuplaTokenProviderImpl(
    private val httpClient: HttpClient,
    private val gateConfig: GateConfig,
    private val tokenRepo: SuplaTokenRepo,
) : SuplaTokenProvider {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var expiresAtEpochMs: Long = 0L

    @Volatile
    private var refreshToken: String? = gateConfig.refreshToken

    @Volatile
    private var loadedFromStore = false

    override suspend fun getAccessToken(forceRefresh: Boolean): String = mutex.withLock {
        ensureLoadedFromStore()

        if (!forceRefresh) {
            val cached = cachedAccessToken
            val now = Clock.System.now().toEpochMilliseconds()
            if (cached != null && now < expiresAtEpochMs) {
                return cached
            }
        }

        return when {
            !refreshToken.isNullOrBlank() && canRefresh() -> refreshWithFallbackLocked()
            !gateConfig.accessToken.isNullOrBlank() -> useStaticAccessTokenLocked()
            else -> throw IllegalStateException(
                "Brak SUPLA tokenu. Ustaw SUPLA_REFRESH_TOKEN (OAuth z offline_access) " +
                    "lub użyj POST /gate/supla/exchange-code (SUPLA_OAUTH_HELPER=true).",
            )
        }
    }

    override suspend fun ensureFreshToken() {
        if (!canRefresh() && readLiveRefreshToken().isNullOrBlank() && refreshToken.isNullOrBlank()) {
            return
        }
        val needsRefresh = mutex.withLock {
            ensureLoadedFromStore()
            val now = Clock.System.now().toEpochMilliseconds()
            cachedAccessToken.isNullOrBlank() ||
                now >= expiresAtEpochMs ||
                (expiresAtEpochMs - now) < REFRESH_AHEAD_MS
        }
        if (needsRefresh) {
            println("SUPLA: proaktywne odświeżenie access tokenu")
            getAccessToken(forceRefresh = true)
        }
    }

    override suspend fun storeTokens(
        accessToken: String,
        refreshToken: String?,
        expiresInSeconds: Long,
    ) = mutex.withLock {
        cachedAccessToken = accessToken
        expiresAtEpochMs = Clock.System.now().toEpochMilliseconds() +
            (expiresInSeconds - SKEW_SECONDS).coerceAtLeast(60) * 1000
        if (!refreshToken.isNullOrBlank()) {
            this.refreshToken = refreshToken
        }
        persistLocked()
        loadedFromStore = true
    }

    private suspend fun refreshWithFallbackLocked(): String {
        val firstError = runCatching { refreshAccessTokenLocked() }.exceptionOrNull()
        if (firstError == null) {
            return cachedAccessToken
                ?: throw IllegalStateException("SUPLA refresh OK, ale brak access_token w cache")
        }

        val previousRefresh = refreshToken
        reloadFromStoreLocked()
        if (!refreshToken.isNullOrBlank() && refreshToken != previousRefresh && canRefresh()) {
            println("SUPLA: retry z refresh_token przeładowanym z DB")
            runCatching { return refreshAccessTokenLocked() }
        }

        val cached = cachedAccessToken
        val now = Clock.System.now().toEpochMilliseconds()
        if (cached != null && now < expiresAtEpochMs) {
            println("SUPLA: refresh nieudany, używam wciąż ważnego access tokenu z DB")
            return cached
        }

        val envRefresh = readLiveRefreshToken()
        if (!envRefresh.isNullOrBlank() && envRefresh != refreshToken && canRefresh()) {
            println("SUPLA: retry z SUPLA_REFRESH_TOKEN z .env/getenv")
            refreshToken = envRefresh
            runCatching { return refreshAccessTokenLocked() }
        }

        if (!gateConfig.accessToken.isNullOrBlank()) {
            println("SUPLA: refresh nieudany (${firstError.message}), używam GATE_ACCESS_TOKEN")
            return useStaticAccessTokenLocked()
        }

        throw firstError
    }

    private fun useStaticAccessTokenLocked(): String {
        cachedAccessToken = gateConfig.accessToken
        expiresAtEpochMs = Long.MAX_VALUE
        return gateConfig.accessToken!!
    }

    private suspend fun ensureLoadedFromStore() {
        if (loadedFromStore) return
        reloadFromStoreLocked()
        loadedFromStore = true
    }

    private suspend fun reloadFromStoreLocked() {
        val stored = runCatching { tokenRepo.load() }.getOrNull()
        if (stored != null) {
            if (!stored.accessToken.isNullOrBlank()) {
                cachedAccessToken = stored.accessToken
            }
            if (!stored.refreshToken.isNullOrBlank()) {
                refreshToken = stored.refreshToken
            }
            stored.accessTokenExpiresAtEpochMs?.let {
                expiresAtEpochMs = it
            }
            println("SUPLA: wczytano tokeny z bazy (refresh=${!stored.refreshToken.isNullOrBlank()})")
        } else {
            val envRefresh = readLiveRefreshToken()
            if (!envRefresh.isNullOrBlank()) {
                refreshToken = envRefresh
                println("SUPLA: używam SUPLA_REFRESH_TOKEN z env (brak dokumentu w DB)")
            }
        }
    }

    private fun canRefresh(): Boolean =
        !gateConfig.clientId.isNullOrBlank() &&
            !gateConfig.clientSecret.isNullOrBlank() &&
            gateConfig.tokenUrl.isNotBlank()

    private suspend fun refreshAccessTokenLocked(): String {
        val currentRefresh = refreshToken
            ?: throw IllegalStateException("Brak refresh_token SUPLA")

        val response = httpClient.submitForm(
            url = gateConfig.tokenUrl,
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", currentRefresh)
                append("client_id", gateConfig.clientId!!)
                append("client_secret", gateConfig.clientSecret!!)
            },
        )

        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.bodyAsText() }.getOrDefault("")
            val expired = errorBody.contains("invalid_grant", ignoreCase = true) ||
                errorBody.contains("expired", ignoreCase = true)
            throw IllegalStateException(
                if (expired) {
                    "SUPLA refresh_token wygasł lub został zrotowany. " +
                        "Wygeneruj nowy (SUPLA_OAUTH_HELPER=true + POST /gate/supla/exchange-code) " +
                        "albo zaktualizuj SUPLA_REFRESH_TOKEN w .env / tabeli supla_token. " +
                        "Szczegóły: ${response.status} $errorBody".trim()
                } else {
                    "SUPLA token refresh failed ${response.status}: $errorBody".trim()
                },
            )
        }

        val body = json.decodeFromString<SuplaTokenResponse>(response.bodyAsText())
        cachedAccessToken = body.accessToken
        expiresAtEpochMs = Clock.System.now().toEpochMilliseconds() +
            (body.expiresIn - SKEW_SECONDS).coerceAtLeast(60) * 1000

        // SUPLA rotuje refresh_token – bez zapisu po restarcie stary przestaje działać
        if (!body.refreshToken.isNullOrBlank()) {
            refreshToken = body.refreshToken
        }

        persistLocked()
        println(
            "SUPLA: access token odświeżony, wygasa za ~${body.expiresIn}s" +
                if (!body.refreshToken.isNullOrBlank()) ", refresh_token zaktualizowany w DB" else "",
        )
        return body.accessToken
    }

    private suspend fun persistLocked() {
        tokenRepo.save(
            SuplaStoredTokens(
                accessToken = cachedAccessToken,
                refreshToken = refreshToken,
                accessTokenExpiresAtEpochMs = expiresAtEpochMs
                    .takeIf { it != Long.MAX_VALUE && it > 0L },
            ),
        )
    }

    companion object {
        private const val SKEW_SECONDS = 60L
        private const val REFRESH_AHEAD_MS = 5L * 60L * 1000L

        /**
         * gateConfig/System.getenv są zamrożone od startu procesu.
         * Przy awarii czytamy plik .env z dysku, żeby podjąć świeży SUPLA_REFRESH_TOKEN bez restartu.
         */
        fun readLiveRefreshToken(): String? {
            val fromFile = sequenceOf(
                File(".env"),
                File("../.env"),
                File("../../.env"),
            ).mapNotNull { file ->
                if (!file.isFile) return@mapNotNull null
                file.readLines()
                    .map { it.trim() }
                    .firstOrNull { it.startsWith("SUPLA_REFRESH_TOKEN=") }
                    ?.substringAfter("=", "")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            }.firstOrNull()

            return fromFile
                ?: System.getenv("SUPLA_REFRESH_TOKEN")?.takeIf { it.isNotBlank() }
        }
    }
}

@Serializable
private data class SuplaTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 3600,
)
