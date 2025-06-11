package com.peekr.common.plugin

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configureAPIDocuments() {
    // Install and configure the "OpenApi" Plugin
    install(OpenApi) {
        outputFormat = OutputFormat.JSON

        // configure basic information about the api
        info {
            title = "Peekr API"
            description = "Peekr API with Swagger-UI"
        }
        // configure the servers from where the api is being served
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        server {
            url = "not yet"
            description = "Production Server"
        }
    }
}
