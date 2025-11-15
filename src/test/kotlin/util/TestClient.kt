package com.peekr.util

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder

/**
 * 클라이언트 테스트 도구
 */
object TestClientFactory {
    fun ApplicationTestBuilder.createTestClient(): HttpClient =
        createClient {
            install(ContentNegotiation) {
                json()
            }
        }
}
