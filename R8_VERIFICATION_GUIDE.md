# R8 / ProGuard Verification Guide — MultiTool KMP

> **Project**: `org.arun.multitool`  
> **DTO under test**: `org.arun.multitool.data.User` (`@Serializable`)  
> **Gradle**: 8.14.3 · **Build type**: `release` (`isMinifyEnabled = true`, `isShrinkResources = true`)

---

## Step 1 — Build the Release APK / AAB

```bash
# From the project root:
cd /Users/arungohil/Desktop/Arun/OthProj/KMP/MultiTool

# APK (faster, good for testing)
./gradlew :composeApp:assembleRelease

# AAB (for Play Store upload)
./gradlew :composeApp:bundleRelease
```

**Expected output locations:**

| Artifact | Path |
|----------|------|
| APK | `composeApp/build/outputs/apk/release/composeApp-release.apk` |
| AAB | `composeApp/build/outputs/bundle/release/composeApp-release.aab` |
| **mapping.txt** | `composeApp/build/outputs/mapping/release/mapping.txt` |
| **usage.txt** | `composeApp/build/outputs/mapping/release/usage.txt` |
| **seeds.txt** | `composeApp/build/outputs/mapping/release/seeds.txt` |
| **configuration.txt** | `composeApp/build/outputs/mapping/release/configuration.txt` |

> If the build **fails** with missing class warnings, jump to **Step 6**.

---

## Step 2 — Confirm R8 Is Actually Applied

### 2a. Check the build output
Look for this line in the Gradle console output:
```
> Task :composeApp:minifyReleaseWithR8
```
If you see `minifyReleaseWithR8`, R8 is running. If this task is missing, `isMinifyEnabled` is not set to `true`.

### 2b. Compare APK sizes
```bash
# Debug APK size
ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Release APK size
ls -lh composeApp/build/outputs/apk/release/composeApp-release.apk
```
The release APK should be **significantly smaller** (often 40–60% smaller). If sizes are similar, R8 may not be stripping unused code.

### 2c. Verify obfuscation via mapping.txt
```bash
# Check that mapping.txt exists and is non-empty
wc -l composeApp/build/outputs/mapping/release/mapping.txt
```
A valid `mapping.txt` will have thousands of lines showing `original → obfuscated` class/method mappings. If the file is empty or missing, obfuscation is not active.

### 2d. Inspect the APK with Android Studio
1. **Build → Analyze APK…** → select the release APK.
2. Check the **DEX file size** — it should be smaller than debug.
3. Browse the class tree — obfuscated classes appear as `a.class`, `b.class`, etc.
4. Confirm your kept classes (`User`, `UserEntity`, Room classes) appear with their **original names**.

---

## Step 3 — Validate Kotlin Serialization DTOs Are Preserved

### 3a. Search mapping.txt for the User DTO
```bash
grep "org.arun.multitool.data.User" composeApp/build/outputs/mapping/release/mapping.txt
```

**✅ Correct result** — The class should appear **without** a `->` rename (or map to itself):
```
org.arun.multitool.data.User -> org.arun.multitool.data.User:
```

**❌ Problem** — If you see it renamed:
```
org.arun.multitool.data.User -> a.b.c:
```
The `-keep` rules are not working. Check `proguard-rules.pro`.

### 3b. Verify the $$serializer companion is preserved
```bash
grep "serializer" composeApp/build/outputs/mapping/release/mapping.txt | grep -i "user"
```
You should see entries for `User$$serializer` and `User$Companion` — these must NOT be stripped.

### 3c. Check usage.txt for removed classes
```bash
grep "org.arun.multitool.data" composeApp/build/outputs/mapping/release/usage.txt
```
`usage.txt` lists classes/members that R8 **removed**. If `User`, `User$$serializer`, or `User$Companion` appear here, your keep rules are failing.

### 3d. Check seeds.txt for kept classes
```bash
grep "org.arun.multitool.data" composeApp/build/outputs/mapping/release/seeds.txt
```
`seeds.txt` lists classes that matched a `-keep` rule. `User` and its serializer should appear here.

---

## Step 4 — Runtime Validation (Install & Test)

### 4a. Install the release APK
```bash
adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk
```

### 4b. Trigger serialization
Navigate to the part of the app that calls `getRandomUser()` (the Ktor call to `jsonplaceholder.typicode.com/users/1`). This exercises:
- Ktor HTTP client → JSON response
- `kotlinx.serialization.json.Json.decodeFromString` → `User(id, name, email)`

### 4c. Watch Logcat for crashes
```bash
adb logcat -s "AndroidRuntime" | grep -E "FATAL|SerializationException|ClassNotFoundException|NoSuchFieldError"
```

**Common R8-caused serialization crashes:**

| Exception | Cause | Fix |
|-----------|-------|-----|
| `kotlinx.serialization.SerializationException: Serializer for class 'User' is not found` | `User$$serializer` was stripped | Add `-keep class org.arun.multitool.data.User$$serializer { *; }` |
| `java.lang.NoSuchFieldError: Companion` | `User$Companion` was obfuscated/stripped | Ensure `-keepclassmembers` for `Companion` is present |
| `java.lang.ClassNotFoundException` | Entire class was removed by tree-shaking | Add explicit `-keep class ...` rule |
| `kotlinx.serialization.MissingFieldException: Field 'name' is required` | Field names were obfuscated (`name` → `a`) | Ensure `-keepattributes` includes `Signature` |
| `IllegalArgumentException: ... is not a synthetic` | Internal serializer structure broken | Use the `-if @Serializable ... -keep $$serializer` pattern |

### 4d. Quick automated smoke test
If you want a programmatic check, add a temporary test in your Android source:
```kotlin
import kotlinx.serialization.json.Json
import org.arun.multitool.data.User

fun verifySerializationAfterR8() {
    val json = """{"id":1,"name":"Leanne Graham","email":"Sincere@april.biz"}"""
    val user = Json.decodeFromString<User>(json)
    assert(user.id == 1) { "id mismatch" }
    assert(user.name == "Leanne Graham") { "name mismatch" }
    assert(user.email == "Sincere@april.biz") { "email mismatch" }
    
    val reEncoded = Json.encodeToString(User.serializer(), user)
    assert(reEncoded.contains("Leanne Graham")) { "re-encoding lost data" }
    
    println("✅ Kotlin Serialization works correctly after R8!")
}
```

---

## Step 5 — Inspect Reports in Detail

### 5a. mapping.txt — Obfuscation map
```bash
# Show the first 50 lines to understand the format
head -50 composeApp/build/outputs/mapping/release/mapping.txt
```
Format:
```
original.package.ClassName -> obfuscated.name:
    type originalField -> obfuscatedField
    type originalMethod(params) -> obfuscatedMethod
```
**Use this file to de-obfuscate stack traces.** Upload it to Play Console alongside your AAB.

### 5b. usage.txt — What R8 removed
```bash
# Count removed items
wc -l composeApp/build/outputs/mapping/release/usage.txt

# Check if any serialization-related classes were removed
grep -i "serial" composeApp/build/outputs/mapping/release/usage.txt
```
If serialization classes appear here, your keep rules need strengthening.

### 5c. seeds.txt — What matched keep rules
```bash
# Verify your critical classes are seeded
grep -E "(User|Room|Screen|ktor|koin)" composeApp/build/outputs/mapping/release/seeds.txt
```

### 5d. configuration.txt — Merged ProGuard config
```bash
# See all rules R8 is using (your rules + library consumer rules)
wc -l composeApp/build/outputs/mapping/release/configuration.txt

# Check if your custom rules are included
grep "org.arun.multitool" composeApp/build/outputs/mapping/release/configuration.txt
```

---

## Step 6 — Troubleshooting & Additional Rules

### If the build fails with warnings
Add specific `-dontwarn` rules for the reported classes:
```proguard
# Example: suppress warnings for missing optional dependencies
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
```

### If serialization crashes at runtime
Add these **additional** rules to `proguard-rules.pro`:

```proguard
# Nuclear option: keep ALL classes annotated with @Serializable
-keep @kotlinx.serialization.Serializable class * {
    *;
}

# Keep the serialization core — enum serializers, polymorphic, etc.
-keep class kotlinx.serialization.internal.** { *; }
-keep class kotlinx.serialization.builtins.** { *; }
```

### If Ktor content negotiation fails
```proguard
# Keep Ktor serialization bridge
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class io.ktor.serialization.** { *; }
```

### If Room queries crash
```proguard
# Keep generated Room DAO implementations
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class *_Impl { *; }  # Room generates Foo_Impl classes
```

---

## How to Interpret Results — Quick Decision Tree

```
Build succeeds?
├── YES → APK size smaller than debug?
│   ├── YES → R8 is stripping code ✅
│   │   └── mapping.txt has entries?
│   │       ├── YES → Obfuscation active ✅
│   │       └── NO  → Add: isMinifyEnabled = true (already set)
│   └── NO  → Check isMinifyEnabled and isShrinkResources
│
├── NO (warnings) → Read the warnings:
│   ├── "Missing class" → Add -dontwarn for optional deps
│   └── "Unresolved reference" → A dependency needs a consumer rule
│
└── Builds but crashes at runtime?
    ├── SerializationException → Check mapping.txt for DTO renaming
    ├── ClassNotFoundException → Class was tree-shaken; add -keep
    └── NoSuchFieldError → Companion/serializer stripped; check rules
```

---

## Summary Checklist

- [ ] `./gradlew :composeApp:assembleRelease` builds without errors
- [ ] `minifyReleaseWithR8` task appears in build log
- [ ] Release APK is significantly smaller than debug
- [ ] `mapping.txt` is non-empty (obfuscation working)
- [ ] `User` class appears **un-renamed** in `mapping.txt`
- [ ] `User$$serializer` is **not** listed in `usage.txt` (not removed)
- [ ] App installs and runs without `SerializationException`
- [ ] JSON deserialization of `User` works correctly at runtime
- [ ] `mapping.txt` uploaded to Play Console for crash de-obfuscation

