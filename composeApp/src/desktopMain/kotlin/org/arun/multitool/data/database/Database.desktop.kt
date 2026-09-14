package org.arun.multitool.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbDir = File(System.getProperty("user.home"), ".multitool")
    if (!dbDir.exists()) dbDir.mkdirs()
    val dbFile = File(dbDir, "multitool.db")
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}