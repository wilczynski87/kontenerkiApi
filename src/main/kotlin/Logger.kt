package com.kontenery

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.LoggerFactory

fun Application.logger() {
    install(CallLogging)
}