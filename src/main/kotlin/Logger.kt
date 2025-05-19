package com.kontenery

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*

fun Application.logger() {
    install(CallLogging)
}