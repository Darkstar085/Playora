<p align="center">
  <h1>Playora</h1>
</p>

<p align="center">
  A modern, network-free video player for Android designed for smooth playback, total privacy, and intuitive gesture controls.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API 24+">
  <img src="https://img.shields.io/badge/Kotlin-2.4.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <br>
  <img src="https://img.shields.io/github/v/release/Darkstar085/Playora?style=for-the-badge&label=Latest%20Release" alt="Latest release">
  <img src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/Darkstar085/Playora/main/.github/badges/downloads.json&style=for-the-badge&label=APK%20Downloads" alt="APK downloads">
  <a href="https://github.com/Darkstar085/Playora/actions/workflows/release.yml"><img src="https://img.shields.io/github/actions/workflow/status/Darkstar085/Playora/release.yml?style=for-the-badge&label=Build" alt="Build status"></a>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License">
</p>

---

## ✨ Features

- 🎥 **Smooth Video Playback** — Built on AndroidX Media3 ExoPlayer with hardware acceleration and
  support for all popular video and audio formats.
- 📴 **100% Network-Free & Offline** — Completely offline media player with zero trackers, telemetry,
  or network permissions.
- 🔒 **Built-in Privacy Locker** — Secure sensitive videos behind Biometric authentication
  (Fingerprint / Face Unlock) or a custom security PIN.
- 👆 **Intuitive Gesture Controls** — Swipe left for brightness, swipe right for volume, horizontal
  swipe to seek, double-tap to seek/play-pause, and pinch to zoom.
- 💬 **Advanced Subtitle Support** — Support for embedded and external subtitles (`.srt`, `.vtt`,
  `.ass`, `.ssa`, `.ttml`), custom encoding, subtitle delay, font styling, and speed adjustments.
- 🎧 **Audio & Background Playback** — Multi-audio track switching, audio boost, background playback
  service, and Picture-in-Picture (PiP) mode.
- 📁 **Smart Media Library** — View videos by folder tree, folders, or flat video list, with
  customizable grid/list view and sorting options.
- ⚡ **Quick Resume & History** — Seamless playback position memory, recent played tracking, and
  custom resume behavior.
- 🎨 **Material 3 Expressive UI** — Clean and responsive Jetpack Compose interface with dynamic color
  theming, dark mode, and customizable player controls.
- ⚙️ **Settings Backup & Restore** — Export and import your customized player and app preferences
  easily.

## 🧰 Tech Stack

| Area                    | Technology                                                                 |
|-------------------------|----------------------------------------------------------------------------|
| Language                | Kotlin 2.4.21, Java 17                                                     |
| Android                 | Min API 24 (Android 7.0), compile / target API 37                          |
| Build                   | Gradle 9.9.0, Android Gradle Plugin 9.5.1, KSP 2.3.13                      |
| UI                      | Jetpack Compose (BOM 2026.09.00), Material 3 1.5.0-alpha15, Navigation 3   |
| Media Engine            | AndroidX Media3 1.11.1 (ExoPlayer, Session, UI), NextLib Extended Media3 Extensions |
| Dependency Injection    | Koin 4.2.2 (Core, Android, Compose ViewModel, Annotations)                 |
| Local Storage           | Room 2.8.5, DataStore Core 1.3.0                                           |
| Async & Background Work | Kotlin Coroutines / Flow 1.11.0, Foreground Service                        |
| Serialization           | Kotlinx Serialization 1.12.0                                               |
| Testing                 | JUnit 4.13.2                                                               |

## 🗂️ Project Structure

The project is structured into clear core domains, features, and navigation modules:

```text
.
├── .github/
│   ├── badges/                         # Generated APK download badge data
│   ├── scripts/                        # Release, badge, and cleanup scripts
│   └── workflows/                      # Debug, release, badge, and cleanup workflows
├── app/
│   ├── build.gradle.kts                # App build configuration and dependencies
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/                    # App icons, drawables, strings, XML resources
│       │   └── kotlin/com/sipun/playora/
│       │       ├── MainActivity.kt
│       │       ├── PlayoraApplication.kt
│       │       ├── core/
│       │       │   ├── common/         # Utilities, extensions, context helpers
│       │       │   ├── data/           # Repositories and data mappers
│       │       │   ├── database/       # Room database, entities, and DAOs
│       │       │   ├── datastore/      # DataStore serializers and preferences sources
│       │       │   ├── domain/         # Media use cases and business logic
│       │       │   ├── media/          # MediaStore service and query runners
│       │       │   ├── model/          # Domain models and preference enums
│       │       │   └── ui/             # Reusable UI components and theme
│       │       ├── feature/
│       │       │   ├── player/         # Video player controls, gestures, and service
│       │       │   ├── settings/       # App and player preference screens
│       │       │   └── videopicker/    # Media picker, search, and privacy vault
│       │       └── navigation/         # Navigation backstack and transitions
│       └── test/
│           └── kotlin/com/sipun/playora/
│               └── core/               # Unit tests (models, utils, serializers)
├── gradle/
│   ├── libs.versions.toml              # Centralized dependency catalog
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

### Architecture Overview

- **`core`** — Base infrastructure including Room database persistence, DataStore preferences,
  MediaStore scanning, domain use cases, and common UI design components.
- **`feature/player`** — Player interface, gestures (swipe volume/brightness/seeking), subtitle
  rendering, and background audio/PiP playback service.
- **`feature/videopicker`** — Media browsing (Folders, Tree, Videos), search filtering, media
  details, and the biometric-protected Privacy locker.
- **`feature/settings`** — Comprehensive customization settings for player controls, subtitles,
  gestures, audio, and configuration backup/restore.
- **`navigation`** — Compose Navigation 3 architecture handling backstack management and animated
  transitions.

## 🚀 Quick Start

### Requirements

- Android Studio
- JDK 17
- Android SDK 37 for development
- Android 7.0 (API 24) or newer for the app

### Clone

```bash
git clone https://github.com/Darkstar085/Playora.git
cd Playora
```

### Build

```bash
./gradlew assembleDebug
```

The generated debug APK is available under `app/build/outputs/apk/debug/`.

## 📦 Releases

Download the latest APK from
the [latest release](https://github.com/Darkstar085/Playora/releases/latest), or browse all
available builds on the [Releases](https://github.com/Darkstar085/Playora/releases) page.

## 🔐 Permissions

Playora operates 100% offline and requires no internet access. Only local system permissions are
used:

- **Storage / Read Media Video** — Required to scan and display local video files and folders.
- **Biometric / Fingerprint** — Used to protect and authenticate access to the Privacy vault.
- **Foreground Service & Notification** — Used for background audio playback and playback
  notification controls.

## 🤝 Contributing

Bug reports, suggestions, and improvements are welcome. Please keep contributions focused and follow
the existing Kotlin and Jetpack Compose conventions.

## 📄 License

Playora is licensed under the [MIT License](LICENSE).

---

<p align="center">
  <strong>Playora</strong> — Smooth, private, and powerful video playback.
</p>
