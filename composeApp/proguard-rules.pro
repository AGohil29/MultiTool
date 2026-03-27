# 1. Keep Kotlin Serialization (CRITICAL)
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.json.** { *; }
-keepclassmembers class ** {
    *** Companion;
    *** $serializer;
}

# 2. Keep Room Database (Prevents "Table not found" errors)
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity

# 3. Keep Voyager (Prevents "Screen not found" errors)
-keep class * extends cafe.adriel.voyager.core.screen.Screen
-keep class * implements cafe.adriel.voyager.core.screen.Screen