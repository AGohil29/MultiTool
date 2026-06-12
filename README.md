This is a Kotlin Multiplatform project targeting Android, iOS.

## Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      MultiTool (Root)                       │
│                      settings.gradle.kts                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    composeApp Module                   │  │
│  │           (Shared Kotlin Multiplatform Code)           │  │
│  │                                                       │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │               commonMain                        │  │  │
│  │  │  Shared business logic, UI, data, and DI        │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │        ▲               ▲               ▲              │  │
│  │  ┌─────────┐    ┌─────────────┐   ┌──────────┐       │  │
│  │  │androidMain│   │ desktopMain │   │ iosMain  │       │  │
│  │  │ (Android) │   │   (JVM)     │   │ (iOS)    │       │  │
│  │  └─────────┘    └─────────────┘   └──────────┘       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    iosApp Module                       │  │
│  │          (Native iOS Entry Point — SwiftUI)            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Target Platforms

| Platform | Source Set      | HTTP Engine  | Entry Point              |
|----------|-----------------|--------------|--------------------------|
| Android  | `androidMain`   | OkHttp       | `MainActivity`           |
| iOS      | `iosMain`       | Darwin       | `iosApp/` (SwiftUI)      |
| Desktop  | `desktopMain`   | OkHttp       | `main.kt`                |

### Package Structure (`commonMain`)

```
org.arun.multitool/
├── di/                  # Dependency injection (Koin modules & initialization)
├── data/                # Data layer
│   ├── database/        #   Room database setup
│   ├── ApiClient        #   Ktor-based HTTP client
│   ├── UserDao          #   Room DAO for user persistence
│   ├── UserEntity       #   Room entity model
│   └── AuthState        #   Authentication state model
├── repository/          # Repository layer (MainRepository, UserRepository)
├── hardware/            # Hardware abstraction (LocationService)
├── library/             # External service abstractions (PaymentGateway)
├── ui/                  # Presentation layer
│   ├── screens/         #   Full screens (List, UserDetail, CheckIn, Settings, CameraCapture)
│   ├── viewmodels/      #   ViewModels (CheckInViewModel, TimerViewModel)
│   ├── components/      #   Reusable UI components (AnimatedProfileHeader, HapticManager, etc.)
│   ├── common/          #   Common UI utilities (Extensions, PermissionErrorUI, etc.)
│   └── transition/      #   Shared element transitions
├── utils/               # Utility classes (PermissionHandler, GarbageCollector)
├── App.kt               # Compose Multiplatform app root
├── MainContainer.kt     # Main navigation container
├── Theme.kt             # App theming (Material 3)
├── Platform.kt          # expect/actual platform declarations
└── NetworkResult.kt     # Sealed class for network response states
```

### Technology Stack

| Category                | Library / Tool                        |
|-------------------------|---------------------------------------|
| **Language**            | Kotlin 2.3.0                          |
| **UI Framework**        | Compose Multiplatform 1.10.0          |
| **Design System**       | Material 3 + Adaptive Layout          |
| **Navigation**          | Voyager                               |
| **Networking**          | Ktor 3.0.0                            |
| **Dependency Injection**| Koin                                  |
| **Local Database**      | Room (Multiplatform) + SQLite Bundled |
| **Key-Value Storage**   | Multiplatform Settings                |
| **Date/Time**           | kotlinx-datetime                      |
| **Serialization**       | kotlinx-serialization                 |
| **Swift Interop**       | SKIE                                  |
| **Code Generation**     | KSP                                   |
| **Testing**             | kotlin-test, Turbine, Ktor Mock, Koin Test |

### Architecture Pattern

The project follows a **layered architecture** with clear separation of concerns:

```
┌────────────────────────────────────┐
│     UI Layer (Compose Screens)     │  Screens, Components, Transitions
├────────────────────────────────────┤
│     ViewModel Layer                │  State management & UI logic
├────────────────────────────────────┤
│     Repository Layer               │  Mediates between data sources
├────────────────────────────────────┤
│     Data Layer                     │  API (Ktor), Database (Room),
│                                    │  Settings, Hardware services
└────────────────────────────────────┘
         ▲
         │  Wired together via Koin DI
```

- **UI Layer** — Compose Multiplatform screens with Voyager navigation, Material 3 theming, and adaptive layouts for different form factors.
- **ViewModel Layer** — Lifecycle-aware ViewModels managing screen state and business logic.
- **Repository Layer** — Abstracts and coordinates multiple data sources behind clean interfaces.
- **Data Layer** — Ktor HTTP client for remote APIs, Room for local persistence, Multiplatform Settings for preferences, and `expect`/`actual` declarations for platform-specific hardware access (location, camera, haptics).

### Platform-Specific Implementations

Each platform source set (`androidMain`, `iosMain`, `desktopMain`) provides `actual` implementations for:
- **Database** — Platform-specific Room database builder
- **DI** — Platform-specific Koin module configuration
- **Hardware** — Location services, camera access, haptic feedback
- **UI Components** — Platform-native dialogs and controls
- **Utilities** — Platform-specific helpers

---

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
