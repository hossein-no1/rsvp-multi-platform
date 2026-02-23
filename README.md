## Links

- **Web**: `https://rapidreader-web.pages.dev/`
- **macOS (DMG)**: `https://mega.nz/file/77J1RahB#0bZg94bYqWBQYczDv6FqbvEXdLfJpZM3MGrGobalpDM`

## 1) Intro

**RapidReader** is a Compose Multiplatform app for focused reading using RSVP (Rapid Serial Visual Presentation).
You can start from a **PDF file**, a **link**, or **pasted text**, and the app presents the content word-by-word for distraction-free reading across platforms.

## 2) Targets supported

- **Android** (Compose)
- **Desktop (JVM)**: macOS (DMG), Windows (MSI), Linux (DEB)
- **iOS**: device (arm64) + simulator (arm64)
- **Web**: Kotlin/Wasm + Kotlin/JS browser targets

## 3) Configuration guide

### Prerequisites

- **Gradle wrapper**: use `./gradlew` (or `.\gradlew.bat` on Windows)
- **JDK**:
  - For day-to-day development, a standard JDK is fine.
  - For **Desktop packaging**, you need a full JDK that includes **`jpackage`**. If packaging fails, set `org.gradle.java.home` in `gradle.properties` to that JDK’s home.

### Quick build commands (recommended)

- **Desktop installers (copies into `dist/desktop/`)**

  - macOS:

    ```shell
    ./gradlew :composeApp:exportDesktopMac
    ```

  - Windows:

    ```shell
    .\gradlew.bat :composeApp:exportDesktopWindows
    ```

  - Linux:

    ```shell
    ./gradlew :composeApp:exportDesktopLinux
    ```

- **Desktop (run locally)**

  ```shell
  ./gradlew :composeApp:run
  ```

- **Android (APK)**

  ```shell
  ./gradlew :composeApp:assembleDebug
  ```

- **Web dev server**

  - Wasm (modern browsers):

    ```shell
    ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
    ```

  - JS (wider browser support):

    ```shell
    ./gradlew :composeApp:jsBrowserDevelopmentRun
    ```

### Run targets

- **Android**

  ```shell
  ./gradlew :composeApp:assembleDebug
  ```

- **Desktop (run)**

  ```shell
  ./gradlew :composeApp:run
  ```

- **Desktop (package installers)**

  ```shell
  # Depending on your Compose Desktop plugin version, these may be either:
  # - packageDmg / packageMsi / packageDeb
  # - packageReleaseDmg / packageReleaseMsi / packageReleaseDeb
  ./gradlew :composeApp:packageDmg
  ./gradlew :composeApp:packageMsi
  ./gradlew :composeApp:packageDeb
  ```

  The recommended tasks above (`exportDesktopMac/Windows/Linux`) will build + copy the resulting installer into `dist/desktop/` with consistent filenames:

  - `dist/desktop/RapidReader-1.0.0-mac.dmg`
  - `dist/desktop/RapidReader-1.0.0-windows.msi`
  - `dist/desktop/RapidReader-1.0.0-linux.deb`

- **Web (Wasm — modern browsers)**

  ```shell
  ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
  ```

- **Web (JS — wider browser support)**

  ```shell
  ./gradlew :composeApp:jsBrowserDevelopmentRun
  ```

- **iOS**
  - Open `iosApp` in Xcode and run the iOS app target.