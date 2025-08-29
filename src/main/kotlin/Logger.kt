package com.kontenery

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun Application.logger() {
    install(CallLogging) {
        level = Level.INFO
    }
}