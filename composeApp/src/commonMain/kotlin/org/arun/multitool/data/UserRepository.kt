package org.arun.multitool.data

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.arun.multitool.NetworkResult
import org.arun.multitool.User
import kotlin.time.Clock

class UserRepository(
    private val client: HttpClient,
    private val userDao: UserDao,
    private val settings: Settings
) {
    private val LAST_SYNC_KEY = "last_sync_timestamp"
    private val SYNC_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes

    // 1. Expose a Flow from the Database (SSOT)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers().map { entities ->
        entities.map { User(it.id, it.name, it.email) }     // Map to UI Model
    }

    suspend fun refreshUsersIfNecessary(forceRefresh: Boolean = false) {
        val lastSync = settings.getLong(LAST_SYNC_KEY, 0L)
        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (forceRefresh || currentTime - lastSync > SYNC_INTERVAL_MS) {
            val result = refreshUsers()
            if (result is NetworkResult.Success) {
                settings.putLong(LAST_SYNC_KEY, currentTime)
            }
        }
    }

    // 2. The Sync Logic: Fetch from Network -> Save to DB
    private suspend fun refreshUsers(): NetworkResult<Unit> {
        return try {
            // Fetch list of users from API
            val users: List<User> = client.get("https://jsonplaceholder.typicode.com/users").body()

            // Map Network Model (User) to Database Entity (UserEntity)
            // switch to a background thread in case of large datasets.
            val entities = withContext(Dispatchers.Default) {
                users.map { networkUser ->
                    UserEntity(
                        id = networkUser.id,
                        name = networkUser.name,
                        email = networkUser.email
                    )
                }
            }

            // Save to DB (This triggers the Flow in getAllUsers)
            userDao.insertUser(entities)

            NetworkResult.Success(Unit)
        } catch (e: HttpRequestTimeoutException) {
            NetworkResult.Error("The server took too long to respond. Please check your internet.")
        } catch (e: Exception) {
            // Mapping platform-specific network exceptions to a common string
            NetworkResult.Error(e.message ?: "Unknown network error")
        }
    }
}