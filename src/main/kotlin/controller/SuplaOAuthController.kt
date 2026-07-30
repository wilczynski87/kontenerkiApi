package com.kontenery.controller

import com.kontenery.GateConfig
import com.kontenery.service.SuplaTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Jednorazowa wymiana authorization code → tokens.
 * Tokeny trafiają od razu do PostgreSQL (tabela supla_token).
 * Włącz: SUPLA_OAUTH_HELPER=true. Po setupie wyłącz.
 */
fun Route.suplaOAuth(
    gateConfig: GateConfig,
    tokenProvider: SuplaTokenProvider,
    httpClient: HttpClient = HttpClient(),
) {
    val json = Json { ignoreUnknownKeys = true }

    route("/gate/supla") {
        post("/exchange-code") {
            if (!gateConfig.oauthHelperEnabled) {
                return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Endpoint wyłączony (ustaw SUPLA_OAUTH_HELPER=true)"),
                )
            }

            val request = call.receive<SuplaExchangeCodeRequest>()
            require(!gateConfig.clientId.isNullOrBlank() && !gateConfig.clientSecret.isNullOrBlank()) {
                "Brak SUPLA_CLIENT_ID / SUPLA_CLIENT_SECRET"
            }
            require(!gateConfig.redirectUri.isNullOrBlank()) { "Brak SUPLA_REDIRECT_URI" }

            val response = httpClient.submitForm(
                url = gateConfig.tokenUrl,
                formParameters = Parameters.build {
                    append("grant_type", "authorization_code")
                    append("client_id", gateConfig.clientId)
                    append("client_secret", gateConfig.clientSecret)
                    append("redirect_uri", gateConfig.redirectUri)
                    append("code", request.code)
                },
            )

            if (!response.status.isSuccess()) {
                val errorBody = runCatching { response.bodyAsText() }.getOrDefault("")
                return@post call.respond(
                    HttpStatusCode.BadGateway,
                    mapOf("error" to "SUPLA exchange failed: ${response.status} $errorBody".trim()),
                )
            }

            val tokens = json.decodeFromString<SuplaExchangeCodeResponse>(response.bodyAsText())
            tokenProvider.storeTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                expiresInSeconds = tokens.expiresIn,
            )

            call.respond(
                SuplaExchangeCodeResult(
                    message = "Tokeny zapisane w PostgreSQL (supla_token). Ustaw SUPLA_OAUTH_HELPER=false. " +
                        "SUPLA_REFRESH_TOKEN w .env jest tylko fallbackiem przy pustej bazie.",
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    expiresIn = tokens.expiresIn,
                    scope = tokens.scope,
                    targetUrl = tokens.targetUrl,
                ),
            )
        }
    }
}

@Serializable
data class SuplaExchangeCodeRequest(
    val code: String,
)

@Serializable
data class SuplaExchangeCodeResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 3600,
    val scope: String? = null,
    @SerialName("target_url") val targetUrl: String? = null,
)

@Serializable
data class SuplaExchangeCodeResult(
    val message: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long,
    val scope: String? = null,
    @SerialName("target_url") val targetUrl: String? = null,
)
