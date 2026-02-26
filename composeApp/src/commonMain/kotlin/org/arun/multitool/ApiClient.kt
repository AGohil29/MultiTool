package org.arun.multitool

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val httpClient = HttpClient {
    install(Logging){
        // 1. Custom Logger implementation
        logger = object : Logger {
            override fun log(message: String) {
                // In KMP, 'println' is mapped to Logcat (Android)
                // and the Xcode Console (iOS) automatically.
                println("HTTP Client: $message")
            }
        }
        // 2. Control the level (Use HEADERS for production, BODY for dev)
        level = LogLevel.BODY

        // 3. Senior Move: Filter out sensitive endpoints or static assets
        filter { request ->
            request.url.host.contains("")
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15000L
        connectTimeoutMillis = 10000L
        socketTimeoutMillis = 15000L
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
}

@Serializable
data class User(val id: Int, val name: String, val email: String)

suspend fun getRandomUser(): User {
    // Ktor handles the networking on the background thread automatically
    return httpClient.get("https://jsonplaceholder.typicode.com/users/1").body()
}