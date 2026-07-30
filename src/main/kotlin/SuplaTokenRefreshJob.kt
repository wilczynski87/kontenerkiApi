package com.kontenery

import com.kontenery.service.SuplaTokenProvider
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Co 10 minut sprawdza access token SUPLA i odświeża go przed wygaśnięciem.
 * Nowy refresh_token jest zapisywany w PostgreSQL (tabela supla_token).
 */
fun Application.configureSuplaTokenRefresh(tokenProvider: SuplaTokenProvider) {
    monitor.subscribe(ApplicationStarted) {
        launch {
            delay(5.seconds)
            while (isActive) {
                runCatching {
                    tokenProvider.ensureFreshToken()
                }.onFailure { e ->
                    log.error("SUPLA token refresh job error: ${e.message}")
                }
                delay(10.minutes)
            }
        }
    }
}
