package org.arun.multitool.repository

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.arun.multitool.data.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [UserRepository] that run identically on the JVM (Android) and
 * Kotlin/Native (iOS).  All I/O is faked so the tests are fully hermetic.
 *
 * Key techniques:
 *  • `runTest` — provides a [TestCoroutineScheduler] so `delay` / virtual time
 *    is skipped automatically, keeping tests instant.
 *  • `Turbine` — deterministic assertion on `Flow` emissions without
 *    timing-dependent `first()` / `take()` hacks.
 *  • `MockEngine` — Ktor's built-in fake HTTP layer (no real network).
 *  • `MapSettings` — in-memory multiplatform-settings, no disk needed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {

    // ── Fake HTTP responses ─────────────────────────────────────────────
    private val sampleUsersJson = """
        [
            {"id": 1, "name": "Alice", "email": "alice@example.com"},
            {"id": 2, "name": "Bob",   "email": "bob@example.com"}
        ]
    """.trimIndent()

    private fun buildMockClient(
        responseBody: String = sampleUsersJson,
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ): HttpClient {
        val engine = MockEngine {
            respond(
                content = responseBody,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    // ── Tests ───────────────────────────────────────────────────────────

    @Test
    fun `getAllUsers emits empty list before any sync`() = runTest {
        val dao = FakeUserDao()
        val repo = UserRepository(buildMockClient(), dao, MapSettings())

        repo.getAllUsers().test {
            val first = awaitItem()
            assertTrue(first.isEmpty(), "Expected empty list before sync")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshUsersIfNecessary fetches and persists users on first call`() = runTest {
        val dao = FakeUserDao()
        val settings = MapSettings()
        val repo = UserRepository(buildMockClient(), dao, settings)

        repo.getAllUsers().test {
            // Initial emission — empty database
            assertEquals(emptyList(), awaitItem())

            // Trigger a network sync (force = true to bypass time-check)
            repo.refreshUsersIfNecessary(forceRefresh = true)
            advanceUntilIdle()

            // The DAO Flow should now emit the synced users
            val synced = awaitItem()
            assertEquals(2, synced.size)
            assertEquals(User(1, "Alice", "alice@example.com"), synced[0])
            assertEquals(User(2, "Bob", "bob@example.com"), synced[1])

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshUsersIfNecessary skips network when within sync interval`() = runTest {
        val dao = FakeUserDao()
        val settings = MapSettings()
        val repo = UserRepository(buildMockClient(), dao, settings)

        // First call — should actually sync
        repo.refreshUsersIfNecessary(forceRefresh = true)
        advanceUntilIdle()

        // Second call without force — interval hasn't passed, so no new network call
        repo.refreshUsersIfNecessary(forceRefresh = false)
        advanceUntilIdle()

        // DAO should still have exactly the same 2 users (no duplicate insert)
        repo.getAllUsers().test {
            val users = awaitItem()
            assertEquals(2, users.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshUsersIfNecessary handles network error gracefully`() = runTest {
        val dao = FakeUserDao()
        val settings = MapSettings()
        // Build a client that returns a 500 error body
        val failingClient = buildMockClient(
            responseBody = "Internal Server Error",
            statusCode = HttpStatusCode.InternalServerError
        )
        val repo = UserRepository(failingClient, dao, settings)

        // Sync should not crash — it catches the exception internally
        repo.refreshUsersIfNecessary(forceRefresh = true)
        advanceUntilIdle()

        // Database should remain empty (no partial writes)
        repo.getAllUsers().test {
            val users = awaitItem()
            assertTrue(users.isEmpty(), "DB should be empty after failed sync")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `forceRefresh always triggers network call`() = runTest {
        val dao = FakeUserDao()
        val settings = MapSettings()
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            respond(
                content = sampleUsersJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val repo = UserRepository(client, dao, settings)

        repo.refreshUsersIfNecessary(forceRefresh = true)
        advanceUntilIdle()
        repo.refreshUsersIfNecessary(forceRefresh = true)
        advanceUntilIdle()

        assertEquals(2, requestCount, "forceRefresh should always hit the network")
    }
}

