# Sippy

An offline-first water tracking app with animated water friends, built with Kotlin, Jetpack Compose (Material 3), Room, Preferences DataStore, WorkManager and manual dependency injection.

## Project structure

```
Sippy/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── gradle/wrapper/{gradle-wrapper.jar,gradle-wrapper.properties}
├── gradlew, gradlew.bat
├── keystore.properties            # created locally, git-ignored
├── .github/workflows/release.yml
├── docs/
│   ├── PRIVACY_POLICY.md
│   └── RELEASE_GUIDE.md
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/
        │   │   ├── drawable/ (ic_launcher_foreground, ic_notification, ic_more, ic_delete, nav icons)
        │   │   ├── mipmap-anydpi-v26/{ic_launcher.xml,ic_launcher_round.xml}
        │   │   ├── values/{strings.xml,themes.xml,ic_launcher_background.xml}
        │   │   └── xml/{backup_rules.xml,data_extraction_rules.xml}
        │   └── java/com/sippy/app/
        │       ├── SippyApp.kt, MainActivity.kt
        │       ├── data/{Entities.kt,Preferences.kt,Repository.kt}
        │       ├── domain/Logic.kt
        │       ├── di/AppContainer.kt
        │       ├── notifications/{Notifier.kt,ReminderWorker.kt,ReminderScheduler.kt,ReminderReceiver.kt,BootReceiver.kt}
        │       └── ui/{AppViewModel.kt,Navigation.kt,Common.kt,FriendArt.kt}
        │           ├── theme/Theme.kt
        │           └── screens/{Onboarding.kt,Home.kt,Friends.kt,History.kt,Settings.kt,DevOptions.kt}
        └── test/java/com/sippy/app/domain/LogicTest.kt
```

## Build and run

Requirements: Android Studio Ladybug or newer (or JDK 17 + Android SDK Platform 35), Android device/emulator on API 26+.

1. Open the folder in Android Studio, let Gradle sync, then Run → app. Or from a terminal:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

2. Tests: `./gradlew testDebugUnitTest` (reports: `app/build/reports/tests/testDebugUnitTest/index.html`).
3. Lint: `./gradlew lintDebug`.

## Icons

The launcher icon is a vector drawable plus an adaptive icon XML in `app/src/main/res`; no binary assets are needed. To generate PNG densities from the vector in Android Studio: right-click `ic_launcher_foreground.xml` → Generate → Image Asset, or use `mipmap` density folders produced by the Asset Studio wizard. For Play, export a 512×512 PNG from the same vector (e.g. Android Studio's Image Asset preview or a vector → PNG converter).

## Updates

Settings has a **Check for updates** button. The app also checks the latest GitHub release once per day using `https://api.github.com/repos/Cooreo/Sippy/releases/latest`. If a newer tag exists, a **Download update** button opens the release APK in the browser. Nothing is downloaded or installed automatically. Built APKs are kept in `releases/`.

## Developer options

Settings → About → tap "Version 1.0.0 (1)" seven times. The unlocked flag is stored in DataStore. Developer options include mood/animation previews, growth and streak editors, progress simulation (never writes), time override (preview only), test friends, fake history, a test notification, debug overlay, and a lock button.

## Privacy

All data stays on device; see [`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md).
