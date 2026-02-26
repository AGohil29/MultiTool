package org.arun.multitool.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // We will retrieve the context from Koin in the module definition,
    // so we change this function signature or use a helper.
    // The most common senior approach is to let the Module handle the context.
    throw Exception("Use getDatabaseBuilder(context) instead")
}

// Helper function that takes context
fun getAndroidDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
