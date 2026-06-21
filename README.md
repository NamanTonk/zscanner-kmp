# ZScanner - Kotlin Multiplatform Barcode Scanner

`zscanner` is a modern, premium Kotlin Multiplatform Mobile (KMM) library targeting Android and iOS. It offers high-performance camera barcode scanning using Jetpack Compose and Compose Multiplatform.

---

## 🌟 Key Features

* **Dual Camera Modes**: Support for both `FullScreen` previews and `FrameOnly` windowed preview modes.
* **Refined Aesthetics**: 
  - Bold, prominent scanner boundary borders (default `5.dp` width).
  - Premium frame-constrained processing overlay.
  - Dynamic HSL-tailored loader glow shadow matching the active frame theme color (Red, Green, Orange, or Custom).
* **Fully Customizable Loader**:
  - Exposes a `@Composable ZScannerCameraScope.() -> Unit` slot.
  - Automatically falls back to the library's internal `DefaultLoader()`.
  - Empowers developers to build custom loading views, overlays, or status indicators seamlessly.
* **Gallery Scan Support**: Fast and direct barcode scanning via photo picker on both Android and iOS with native loading state orchestration.

---

## 📂 Project Structure

* **[`/zscanner`](./zscanner)**: The core library module containing camera preview rendering, barcode detectors, scan overlays, and state controllers.
* **[`/shared`](./shared)**: The shared module for the demo app, housing state models, view models, and the Compose UI configuration panels.
* **[`/androidApp`](./androidApp)**: Android application launcher and container.
* **[`/iosApp`](./iosApp)**: Xcode entry point and SwiftUI wrapper to launch the application on iOS devices.

---

## 🚀 Getting Started

## 📦 Installation

To use `zscanner` in your Compose Multiplatform project, choose one of the following integration methods:

### Option A: JitPack (Recommended for public/anonymous use)
JitPack allows you to include the library anonymously without needing a GitHub Personal Access Token.

1. **Add Repository**: In the client project's `settings.gradle.kts` (under `dependencyResolutionManagement > repositories`), add JitPack:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```
2. **Add Dependency**: In the client project's common module `build.gradle.kts` (usually `shared/build.gradle.kts` or `composeApp/build.gradle.kts`), add:
   ```kotlin
   sourceSets {
       commonMain.dependencies {
           implementation("com.github.NamanTonk:zscanner-kmp:1.0.0")
       }
   }
   ```

### Option B: GitHub Packages
GitHub Packages requires authentication even for public packages.

1. **Add Repository**: In the client project's `settings.gradle.kts` (under `dependencyResolutionManagement > repositories`), add the repository:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven {
               url = uri("https://maven.pkg.github.com/NamanTonk/zscanner-kmp")
               credentials {
                   username = "YOUR_GITHUB_USERNAME"
                   password = "YOUR_GITHUB_PERSONAL_ACCESS_TOKEN" // Needs 'read:packages' permission
               }
           }
       }
   }
   ```
2. **Add Dependency**: In the client project's common module `build.gradle.kts` (usually `shared/build.gradle.kts` or `composeApp/build.gradle.kts`), add:
   ```kotlin
   sourceSets {
       commonMain.dependencies {
           implementation("com.github.namantonk:zscanner:1.0.0")
       }
   }
   ```

---

### Customizing the Loader (Example)

To override the default spinner with a custom loading indicator, simply pass your composable to the `loader` parameter in `ZScannerScreen`:

```kotlin
ZScannerScreen(
    onResult = { result -> /* handle result */ },
    onClose = { /* handle close */ },
    permissionController = permissionController,
    onScanFromGallery = { /* launch gallery */ },
    loader = {
        // You can run DefaultLoader() or write custom layouts inside ZScannerCameraScope
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(bottom = 96.dp)
            ) {
                Text(
                    text = "Analyzing barcode...",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
)
```

---

## 🛠️ Verification & Compilation

Validate compilation using the Gradle wrapper:

* **Android Target**:
  ```bash
  ./gradlew :zscanner:compileAndroidMain
  ```
* **iOS Target (Simulator Arm64)**:
  ```bash
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  ```
* **Run Demo Application (Android)**:
  ```bash
  ./gradlew :androidApp:installDebug
  ```