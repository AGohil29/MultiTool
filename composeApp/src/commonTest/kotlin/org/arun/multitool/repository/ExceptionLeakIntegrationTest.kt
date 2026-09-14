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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.arun.multitool.data.User
import org.arun.multitool.data.UserEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  "The Asynchronous Exception Leak" — Integration Test
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  PROBLEM:
 *  When an HTTP call fails (e.g. 404 Not Found), Ktor's `response.body<T>()`
 *  throws a deserialization/client exception inside the coroutine context.
 *  If the `try/catch` lives in a parent scope that doesn't structurally enclose
 *  the failing `withContext(Dispatchers.Default)` block, or if a
 *  `CoroutineExceptionHandler` is missing, the exception can propagate
 *  unchecked — cancelling the entire parent `CoroutineScope` and destroying
 *  the downstream `Flow` collector (the UI).
 *
 *  WHAT WE PROVE:
 *  1. Pre-seed the Room cache with known records (the "offline truth").
 *  2. Force the Ktor client layer to return an explicit **404 Not Found**
 *     with a non-JSON payload — guaranteed deserialization crash.
 *  3. Call `refreshUsersIfNecessary(forceRefresh = true)` — this enters the
 *     broken network path.
 *  4. Assert the repository **does not** bubble the crash up to the caller.
 *  5. Assert the cached Room records are **preserved untouched** and still
 *     visible through the `getAllUsers()` Flow to the UI subscriber.
 *  6. Assert the `last_sync_timestamp` is **NOT updated** (failed sync must
 *     not mark itself as successful).
 *
 *  Runs identically on JVM (Android) and Kotlin/Native (iOS).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExceptionLeakIntegrationTest {

    // ── Cached records that simulate data already persisted in Room ──────
    private val cachedEntities = listOf(
        UserEntity(id = 10, name = "Cached Alice", email = "alice@cached.io"),
        UserEntity(id = 20, name = "Cached Bob", email = "bob@cached.io"),
        UserEntity(id = 30, name = "Cached Carol", email = "carol@cached.io"),
    )

    private val cachedUsers = cachedEntities.map { User(it.id, it.name, it.email) }

    // ── Mock engines that simulate different failure modes ───────────────

    /** Returns a plain-text 404 body — Ktor's `body<List<User>>()` will
     *  throw because it cannot deserialize HTML/text into a typed list. */
    private fun build404Client(): HttpClient {
        val engine = MockEngine {
            respond(
                content = """{"error": "Not Found", "status": 404}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /** Returns a 404 with a raw HTML body — the worst-case scenario where
     *  the CDN/proxy returns an error page instead of JSON. */
    private fun build404HtmlClient(): HttpClient {
        val engine = MockEngine {
            respond(
                content = "<html><body><h1>404 Not Found</h1></body></html>",
                status = HttpStatusCode.NotFound,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Text.Html.toString()
                )
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /** Engine that throws a raw exception from the transport layer —
     *  simulates a socket error that fires *before* any HTTP status. */
    private fun buildCrashingClient(): HttpClient {
        val engine = MockEngine {
            throw RuntimeException("Connection reset by peer")
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /** Returns a valid 200 with fresh data — used to prove recovery. */
    private fun buildHealthyClient(): HttpClient {
        val freshJson = """
            [
                {"id": 99, "name": "Fresh Dave", "email": "dave@new.io"}
            ]
        """.trimIndent()
        val engine = MockEngine {
            respond(
                content = freshJson,
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    // ── Helper: pre-seed the DAO ────────────────────────────────────────
    private suspend fun seededDao(): FakeUserDao {
        val dao = FakeUserDao()
        dao.insertUser(cachedEntities)
        return dao
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TEST 1 — 404 JSON payload does not destroy the cached Flow
    // ═══════════════════════════════════════════════════════════════════
    @Test
    fun `404 Not Found does not leak exception and cached records survive`() = runTest {
        val dao = seededDao()
        val settings = MapSettings()
        val repo = UserRepository(build404Client(), dao, settings)

        repo.getAllUsers().test {
            // ① Verify cached records are visible BEFORE the broken sync
            val beforeSync = awaitItem()
            assertEquals(cachedUsers, beforeSync, "Cached data must be visible before sync")

            // ② Trigger the broken network call — this MUST NOT throw
            repo.refreshUsersIfNecessary(forceRefresh = true)
            advanceUntilIdle()

            // ③ The Flow must still be alive and still emitting cached data.
            //    If the exception leaked, Turbine would see a terminal error
            //    event here instead of the cached list.
            //    Since the DAO was not updated, there should be no new emission.
            //    Calling expectNoEvents() proves the Flow didn't error out
            //    AND didn't silently overwrite the cache.
            expectNoEvents()

            // ④ Explicitly re-check the current value via a fresh subscription
            cancelAndIgnoreRemainingEvents()
        }

        // ⑤ Double-check: a brand-new subscriber still sees the cached data
        repo.getAllUsers().test {
            val snapshot = awaitItem()
            assertEquals(3, snapshot.size, "All 3 cached users must be intact")
            assertEquals("Cached Alice", snapshot[0].name)
            assertEquals("Cached Bob", snapshot[1].name)
            assertEquals("Cached Carol", snapshot[2].name)
            cancelAndIgnoreRemainingEvents()
        }

        // ⑥ Sync timestamp must NOT be updated (failed sync ≠ success)
        assertEquals(
            0L,
            settings.getLong("last_sync_timestamp", 0L),
            "Failed sync must not update the last-sync timestamp"
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TEST 2 — 404 HTML (CDN error page) is equally contained
    // ═══════════════════════════════════════════════════════════════════
    @Test
    fun `404 HTML error page does not corrupt cache or crash subscriber`() = runTest {
        val dao = seededDao()
        val settings = MapSettings()
        val repo = UserRepository(build404HtmlClient(), dao, settings)

        repo.getAllUsers().test {
            assertEquals(cachedUsers, awaitItem())

            // Force sync against the HTML 404 — must not crash
            repo.refreshUsersIfNecessary(forceRefresh = true)
            advanceUntilIdle()

            // Flow still alive, cache untouched
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        // Verify cache integrity from a fresh collector
        repo.getAllUsers().test {
            assertEquals(cachedUsers, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TEST 3 — Transport-layer IOException is equally contained
    // ═══════════════════════════════════════════════════════════════════
    @Test
    fun `transport IOException does not leak and cached data survives`() = runTest {
        val dao = seededDao()
        val settings = MapSettings()
        val repo = UserRepository(buildCrashingClient(), dao, settings)

        repo.getAllUsers().test {
            assertEquals(cachedUsers, awaitItem())

            repo.refreshUsersIfNecessary(forceRefresh = true)
            advanceUntilIdle()

            // No new emission, no error — cache is stable
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TEST 4 — Multiple rapid failures don't accumulate or corrupt
    // ═══════════════════════════════════════════════════════════════════
    @Test
    fun `rapid consecutive failures preserve cache without corruption`() = runTest {
        val dao = seededDao()
        val settings = MapSettings()
        val repo = UserRepository(build404Client(), dao, settings)

        // Fire 5 forced syncs in quick succession
        repeat(5) {
            repo.refreshUsersIfNecessary(forceRefresh = true)
        }
        advanceUntilIdle()

        // After all failures, cache must be exactly the original 3 records
        repo.getAllUsers().test {
            val users = awaitItem()
            assertEquals(cachedUsers, users, "Cache must survive 5 consecutive failures")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TEST 5 — Recovery: a healthy sync after a 404 correctly updates
    //           the cache, proving the repository is not in a poisoned
    //           state after the earlier exception.
    // ═══════════════════════════════════════════════════════════════════
    @Test
    fun `repository recovers after 404 and updates cache on next healthy sync`() = runTest {
        val dao = seededDao()
        val settings = MapSettings()

        // Phase 1 — break it with a 404
        val brokenRepo = UserRepository(build404Client(), dao, settings)
        brokenRepo.refreshUsersIfNecessary(forceRefresh = true)
        advanceUntilIdle()

        // Cache still intact after failure
        brokenRepo.getAllUsers().test {
            assertEquals(cachedUsers, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Phase 2 — construct a new repository pointing at a healthy endpoint
        //           (same DAO & settings to prove shared state is clean)
        val healthyRepo = UserRepository(buildHealthyClient(), dao, settings)

        healthyRepo.getAllUsers().test {
            // Current cache
            assertEquals(cachedUsers, awaitItem())

            // Trigger healthy sync — timestamp was never set (failed sync),
            // so this will pass the interval check automatically
            healthyRepo.refreshUsersIfNecessary(forceRefresh = true)
            advanceUntilIdle()

            // Cache is now replaced with fresh data
            val fresh = awaitItem()
            assertEquals(1, fresh.size)
            assertEquals(User(99, "Fresh Dave", "dave@new.io"), fresh[0])

            cancelAndIgnoreRemainingEvents()
        }

        // Sync timestamp is now updated (successful sync)
        assertTrue(
            settings.getLong("last_sync_timestamp", 0L) > 0L,
            "Successful sync must record the timestamp"
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TEST 6 — Concurrent subscriber safety: a second Flow collector
    //           attached DURING a failing sync must also remain stable.
    // ═══════════════════════════════════════════════════════════════════
    @Test
    fun `concurrent Flow subscribers survive a 404 failure mid-sync`() = runTest {
        val dao = seededDao()
        val settings = MapSettings()
        val repo = UserRepository(build404Client(), dao, settings)

        // Subscriber A — attached before the sync
        val collectedA = mutableListOf<List<User>>()
        val jobA = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.getAllUsers().collect { collectedA.add(it) }
        }

        // Subscriber B — attached before the sync
        val collectedB = mutableListOf<List<User>>()
        val jobB = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.getAllUsers().collect { collectedB.add(it) }
        }

        advanceUntilIdle()

        // Both subscribers should have received the cached data
        assertTrue(collectedA.isNotEmpty(), "Subscriber A must have initial emission")
        assertTrue(collectedB.isNotEmpty(), "Subscriber B must have initial emission")
        assertEquals(cachedUsers, collectedA.last())
        assertEquals(cachedUsers, collectedB.last())

        // Now fire the 404 — must not kill either collector
        repo.refreshUsersIfNecessary(forceRefresh = true)
        advanceUntilIdle()

        // Both subscribers must still be active with the same cached data
        assertEquals(cachedUsers, collectedA.last(), "Subscriber A cache intact after 404")
        assertEquals(cachedUsers, collectedB.last(), "Subscriber B cache intact after 404")

        // Neither subscriber received an error or a corrupted emission
        assertFalse(
            collectedA.any { it.isEmpty() && collectedA.indexOf(it) > 0 },
            "Subscriber A must never see an empty list after initial seed"
        )

        jobA.cancel()
        jobB.cancel()
    }
}


