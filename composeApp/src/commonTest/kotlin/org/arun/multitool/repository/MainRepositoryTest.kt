package org.arun.multitool.repository

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.arun.multitool.data.User
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [MainRepository] — proves that the periodic `startUpdating()` stream
 * emits correctly using **virtual time**.
 *
 * `runTest` auto-skips real delays, but `advanceTimeBy` gives us precise control
 * over the virtual clock so we can assert exact emission ordering without waiting.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainRepositoryTest {

    @Test
    fun `initial state is the default user`() = runTest {
        val repo = MainRepository()

        repo.userFlow.test {
            val initial = awaitItem()
            assertEquals(User(id = 0, name = "Initial", email = "user@gmail.com"), initial)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startUpdating emits sequential updates every 2 seconds`() = runTest {
        val repo = MainRepository()

        repo.userFlow.test {
            // Consume the initial value
            assertEquals("Initial", awaitItem().name)

            // Launch the infinite updating loop in a child coroutine
            val job = launch { repo.startUpdating() }

            // Advance virtual clock by 2 seconds → first update
            advanceTimeBy(2_001)
            assertEquals("User Update #1", awaitItem().name)

            // Advance another 2 seconds → second update
            advanceTimeBy(2_000)
            assertEquals("User Update #2", awaitItem().name)

            // Advance another 2 seconds → third update
            advanceTimeBy(2_000)
            assertEquals("User Update #3", awaitItem().name)

            job.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no emission between update intervals`() = runTest {
        val repo = MainRepository()

        repo.userFlow.test {
            awaitItem() // consume initial

            val job = launch { repo.startUpdating() }

            // Advance only 1 second — should NOT produce a new emission
            advanceTimeBy(1_000)
            expectNoEvents()

            // Now advance the remaining 1 second + a tiny bit → first update
            advanceTimeBy(1_001)
            assertEquals("User Update #1", awaitItem().name)

            job.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

