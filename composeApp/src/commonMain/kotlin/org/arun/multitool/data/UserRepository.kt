package org.arun.multitool.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.arun.multitool.NetworkResult
import org.arun.multitool.User

class UserRepository(
    private val client: HttpClient,
    private val userDao: UserDao,
) {
    // 1. Single Source of Truth: UI observes this Flow
    // Any change in the database triggers an emission here
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    // 2. The Sync Logic: Fetch from Network -> Save to DB
    suspend fun refreshUsers(): NetworkResult<Unit> {
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