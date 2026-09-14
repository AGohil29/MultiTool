package org.arun.multitool.repository

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
import org.arun.multitool.data.User
import org.arun.multitool.data.UserDao
import org.arun.multitool.data.UserEntity
import kotlin.time.Clock

class UserRepository(
    private val client: HttpClient,
    private val userDao: UserDao,
    private val settings: Settings
) {
    private val lastSyncKey = "last_sync_timestamp"
    private val syncIntervalMS = 10 * 60 * 1000L // 10 minutes

    // 1. Expose a Flow from the Database (SSOT)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers().map { entities ->
        entities.map { User(it.id, it.name, it.email) }     // Map to UI Model
    }

    suspend fun refreshUsersIfNecessary(forceRefresh: Boolean = false) {
        val lastSync = settings.getLong(lastSyncKey, 0L)
        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (forceRefresh || currentTime - lastSync > syncIntervalMS) {
            val result = refreshUsers()
            if (result is NetworkResult.Success) {
                settings.putLong(lastSyncKey, currentTime)
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