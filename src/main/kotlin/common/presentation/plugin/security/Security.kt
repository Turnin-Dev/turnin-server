package com.peekr.common.presentation.plugin.security

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication

fun Application.configureSecurity() {
    install(Authentication) {
    }
}
