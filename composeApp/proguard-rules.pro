# ========================
# 1. Kotlin Serialization (CRITICAL for R8/ProGuard)
# ========================
# Preserve annotations, signatures, and inner classes needed by serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Keep the kotlinx.serialization infrastructure
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }

# Keep all @Serializable classes — their fields, constructors, and generated serializers
-keep @kotlinx.serialization.Serializable class ** { *; }

# Keep the synthetic $serializer class generated for every @Serializable class
-keepclassmembers class ** {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep the generated serializer() on the Companion
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep the generated $$serializer class (the actual serializer implementation)
-if @kotlinx.serialization.Serializable class **
-keep class <1>$$serializer { *; }

# Preserve app DTO package explicitly
-keep class org.arun.multitool.data.** { *; }

# ========================
# 2. Room Database
# ========================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ========================
# 3. Voyager Navigation
# ========================
-keep class * extends cafe.adriel.voyager.core.screen.Screen
-keep class * implements cafe.adriel.voyager.core.screen.Screen

# ========================
# 4. Ktor / OkHttp
# ========================
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ========================
# 5. Koin
# ========================
-keep class org.koin.** { *; }
-dontwarn org.koin.**
