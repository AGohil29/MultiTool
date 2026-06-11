package org.arun.multitool.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/my_room.db"
    println("Database Path: $dbFilePath")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        // The factory is now explicitly provided via the generated implementation
        factory = { AppDatabaseConstructor.initialize() }
    )
}