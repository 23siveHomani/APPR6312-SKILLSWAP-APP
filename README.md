# SkillSwap — Part 3 Final PoE Submission

![SkillSwap Banner](https://img.shields.io/badge/SkillSwap-Trade%20Skills%20Not%20Cash-blue?style=for-the-badge)
![Android CI Build](https://github.com/SkillSwap/skillswap/actions/workflows/build.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin%201.9-purple?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026%2B%29-green?style=for-the-badge)

**Trade skills, not cash.**

SkillSwap is a hyperlocal, peer-to-peer skill exchange Android application designed to make learning and service sharing accessible without monetary transactions. Community members can offer skills (such as tutoring, graphic design, repairs, language teaching, and tech support) and request swaps from others nearby. 

> **Student Name:** Asive Homani  
> **Student Number:** ST10441554  
> **Module:** OPSC6312 — Open Source Coding (Intermediate)  
> **Submission:** Part 3 Final Portfolio of Evidence (PoE)  
> **GitHub Repository:** [https://github.com/SkillSwap/skillswap](https://github.com/SkillSwap/skillswap)  
> **Video Demonstration (YouTube Unlisted):** [https://www.youtube.com/watch?v=YOUR_UNLISTED_VIDEO_LINK](https://www.youtube.com/watch?v=YOUR_UNLISTED_VIDEO_LINK)  

---

## Table of Contents

1. [App Purpose & Overview](#app-purpose--overview)
2. [Key Architecture & Design Considerations](#key-architecture--design-considerations)
   - [Authentication & Password Security](#1-authentication--password-security)
   - [Single Sign-On (SSO)](#2-single-sign-on-sso)
   - [Offline Mode with WorkManager Sync](#3-offline-mode-with-workmanager-sync)
   - [Real-time Push Notifications (FCM)](#4-real-time-push-notifications-fcm)
   - [Multi-language Support (South African Languages)](#5-multi-language-support-south-african-languages)
   - [NoSQL Database & Blob Asset Storage](#6-nosql-database--blob-asset-storage)
3. [Release Notes (Changes Since Part 2 Prototype)](#release-notes-changes-since-part-2-prototype)
4. [Evidence of Preparation for Google Play Store Publication](#evidence-of-preparation-for-google-play-store-publication)
5. [GitHub & GitHub Actions CI/CD](#github--github-actions-cicd)
   - [Workflow Configuration](#workflow-configuration)
   - [Git Branching & Final PoE Tagging](#git-branching--final-poe-tagging)
6. [Demonstration Video Walkthrough & Script](#demonstration-video-walkthrough--script)
7. [Code Documentation, Comments & Logging](#code-documentation-comments--logging)
8. [Use of AI Tools Statement](#use-of-ai-tools-statement)
9. [References](#references)

---

## App Purpose & Overview

Many individuals face financial barriers when seeking to learn new skills or obtain everyday services. Existing platforms (like Thumbtack or TaskRabbit) revolve entirely around cash payments. **SkillSwap** bridges this gap by creating a time-credit and skill barter ecosystem:

- **Time-Credit Economy**: Users exchange time and skills directly.
- **Accessible & Inclusive**: Multi-language support ensures inclusivity across South Africa's diverse communities.
- **Offline Reliability**: Full caching and queued offline actions ensure seamless app performance even with unstable mobile data connectivity.

---

## Key Architecture & Design Considerations

The application is built using modern Android architecture components following the **MVVM (Model-View-ViewModel)** and **Repository Pattern**.

```
com.skillswap.app/
├── data/
│   ├── api/          # Retrofit interface and OkHttp JWT auth interceptors
│   ├── local/        # Room Database (cached_listings, pending_sync_items)
│   ├── model/        # DTOs for User, SkillListing, Requests, Responses
│   └── repository/   # AuthRepository, ListingRepository
├── ui/
│   ├── home/         # HomeActivity, HomeViewModel, ListingAdapter
│   ├── login/        # LoginActivity, LoginViewModel
│   ├── register/     # RegisterActivity, RegisterViewModel
│   └── settings/     # SettingsActivity, SettingsViewModel
└── util/
    ├── LocaleHelper             # Dynamic language switching (en, zu, af)
    ├── PasswordUtils            # Password hashing & security helpers
    ├── ServiceLocator           # Dependency provider
    ├── SessionManager           # EncryptedSharedPreferences (AES-256 GCM)
    ├── SkillSwapMessagingService # Firebase Cloud Messaging push handler
    └── SyncWorker               # WorkManager offline sync background task
```

### 1. Authentication & Password Security
- **Password Hashing**: Passwords are sent over encrypted HTTPS connections and hashed server-side using **bcrypt** with salt rounds before storage in the NoSQL database. A client-side SHA-256 helper (`PasswordUtils`) is also available.
- **Secure Token Storage**: The application NEVER stores raw passwords locally. Authentication tokens (JWT) are stored in `SessionManager` using **`EncryptedSharedPreferences`** backed by the Android Keystore (AES-256 GCM).

### 2. Single Sign-On (SSO)
- Integrated **Google Sign-In** via Firebase Auth (`com.google.android.gms:play-services-auth`).
- Users can log in with a single tap using their Google credentials via `loginWithGoogle` and the backend `POST /auth/google` endpoint.

### 3. Offline Mode with WorkManager Sync
- **Local Room Caching**: Live skill listings fetched from the REST API are automatically stored in Room DB (`cached_listings`). When offline, the app displays cached listings with a clear visual banner (`tvOfflineBanner`).
- **Offline Actions**: If a user creates a skill listing while offline, it is stored in Room (`pending_sync_items`).
- **Automatic Synchronization**: A background `SyncWorker` managed by **WorkManager** automatically triggers with a `NetworkType.CONNECTED` constraint to post queued items to the server as soon as connection is restored.

### 4. Real-time Push Notifications (FCM)
- Integrated **Firebase Cloud Messaging** (`SkillSwapMessagingService`).
- Registers device FCM tokens with the backend `PATCH /users/me`.
- Displays real-time status bar notifications via Android's `NotificationManagerCompat` and high-priority notification channels (`skillswap_notifications`).
- Supports runtime Android 13+ (`POST_NOTIFICATIONS`) permission requests.

### 5. Multi-language Support (South African Languages)
- Fully supports three South African languages:
  - **English (`en`)**
  - **isiZulu (`zu`)**
  - **Afrikaans (`af`)**
- Language selection in `SettingsActivity` dynamically updates the application locale via `LocaleHelper` and `AppCompatDelegate.setApplicationLocales`.

### 6. NoSQL Database & Blob Asset Storage
- **REST API & MongoDB Atlas**: Connected to a Node.js/Express backend hosted on Render, backed by a MongoDB Atlas document store.
- **Blob Asset Support**: Skill listings support image and media attachments stored via blob/cloud storage URLs (`imageUrl`).

---

## Release Notes (Changes Since Part 2 Prototype)

### Version 1.0.0 — Final PoE Release

#### 🌟 Innovative & Major Features Added:
1. **Google Single Sign-On (SSO)**:
   - Added "Sign in with Google" button on the login screen.
   - Integrated Google Auth SDK & backend token verification.
2. **Offline Action Queueing & WorkManager Synchronization**:
   - Implemented `PendingSyncEntity` and `PendingSyncDao` in Room.
   - Implemented `SyncWorker` to automatically flush offline creations when connectivity returns.
3. **System Push Notifications**:
   - Built full system notification drawer integration (`NotificationCompat.Builder`) in `SkillSwapMessagingService`.
   - Added notification permission handling (`POST_NOTIFICATIONS`).
4. **Dynamic South African Language Switching**:
   - Added runtime locale switching for **isiZulu (`zu`)** and **Afrikaans (`af`)**.
   - Language preferences persist both locally and on the server profile (`PATCH /users/me`).
5. **Skill Listing Creation & Blob Media Support**:
   - Added Floating Action Button (FAB) and creation dialog (`DialogAddListingBinding`).
   - Supports blob storage URLs for listing images.
6. **Security & Encrypted Token Storage**:
   - Configured `EncryptedSharedPreferences` using AES-256 GCM in Android Keystore.
7. **Google Play Store Release Readiness**:
   - Added ProGuard / R8 shrinking rules in `proguard-rules.pro`.
   - Configured release build configuration and vector drawables.

---

## Evidence of Preparation for Google Play Store Publication

The application is prepared for publication on the Google Play Store in accordance with Google Play Launch Checklists:

### 1. Build Configuration & Target SDK Compliance
- **Target SDK**: Configured for `compileSdk = 34` and `targetSdk = 34` meeting Google Play Store requirements.
- **ProGuard / R8 Obfuscation**: Production code shrinking and obfuscation enabled in `app/proguard-rules.pro` to keep data models intact while stripping unused code.

### 2. Generating a Signed Release APK / AAB
To generate a signed Android App Bundle (`.aab`) or Signed APK for submission:

```bash
cd android

# Generate Signed Debug / Release APK
./gradlew assembleRelease

# Generate Android App Bundle (.aab) for Google Play Console upload
./gradlew bundleRelease
```

Output Artifacts location:
- APK: `android/app/build/outputs/apk/release/app-release.apk`
- AAB: `android/app/build/outputs/bundle/release/app-release.aab`

### 3. Store Listing & Metadata Checklist
- **App Title**: SkillSwap — Trade Skills, Not Cash
- **Category**: Education / Social Networking
- **Content Rating**: Everyone
- **Privacy Policy**: HTTPS policy URL pointing to the Render backend privacy declaration.
- **Screenshots & Assets**: High-resolution phone screenshots prepared for the 3 main user flows:
  1. Login & Google Single Sign-On
  2. Browse Skill Listings with Multi-language support (English, isiZulu, Afrikaans)
  3. Settings & Offline Mode Sync Status

---

## GitHub & GitHub Actions CI/CD

### Workflow Configuration
Automated testing and builds are executed on every `push` and `pull_request` using GitHub Actions.

Workflow file: `.github/workflows/build.yml`
```yaml
name: Android Automated Build & Test (Final PoE)

on:
  push:
    branches: [ main, master, '**' ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    name: Build & Run Unit Tests
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: android

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run automated unit tests
        run: ./gradlew testDebugUnitTest --stacktrace

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: SkillSwap-Debug-APK
          path: android/app/build/outputs/apk/debug/app-debug.apk
```

### Git Branching & Final PoE Tagging
To tag the final submission commit as required by the brief, execute:

```bash
# Tag the final PoE commit
git tag -a v1.0.0-final-poe -m "Final PoE Submission - SkillSwap"

# Push tags to GitHub
git push origin v1.0.0-final-poe
```

---

## Demonstration Video Walkthrough & Script

The demonstration video recorded on a physical mobile phone includes voice-over narration explaining each feature:

### Step-by-Step Video Script:
1. **Introduction (0:00 - 0:30)**:
   - Introduce yourself (Asive Homani, ST10441554) and state the purpose of SkillSwap running on a mobile phone.
2. **Registration & Password Hashing (0:30 - 1:15)**:
   - Demonstrate registering a new account. Show input validation error messages.
   - Explain password security: passwords are bcrypt-hashed server-side and local sessions use AES-256 `EncryptedSharedPreferences`.
3. **Google Single Sign-On (SSO) (1:15 - 1:45)**:
   - Tap "Sign in with Google" and sign in seamlessly.
4. **Multi-language Support (1:45 - 2:30)**:
   - Navigate to Settings. Switch the language to **isiZulu** and then **Afrikaans**. Show the UI instantly updating titles, buttons, and banners.
5. **Browse Skills & Create Listing (2:30 - 3:15)**:
   - Show browsing skill listings with blob image assets. Click the FAB to add a new listing.
6. **Offline Mode with Sync (3:15 - 4:15)**:
   - Turn off Wi-Fi/Mobile Data on the device (Flight Mode).
   - Refresh the home screen to show the **cached listings banner**.
   - Create a new skill listing while offline. Show the **"Listing saved offline. Will automatically sync when reconnected"** toast and pending banner.
   - Turn Wi-Fi/Mobile Data back on. Show **WorkManager** automatically syncing the offline item to MongoDB.
7. **Real-time Push Notifications (4:15 - 4:45)**:
   - Trigger/receive a push notification and show it appearing in the system notification tray.
8. **Database & API Inspection (4:45 - 5:30)**:
   - Show the MongoDB Atlas dashboard / Render logs displaying the stored User documents, hashed passwords, and Skill Listings.

---

## Code Documentation, Comments & Logging

All Kotlin source files contain detailed KDoc comments, citations, and structured Android logging (`Log.i`, `Log.d`, `Log.w`, `Log.e`):
- `HomeActivity.kt`: Logs state transitions, dialog events, and permission callbacks.
- `LoginActivity.kt`: Logs authentication flow, SSO callbacks, and token handling.
- `SyncWorker.kt`: Logs background WorkManager sync events.
- `SkillSwapMessagingService.kt`: Logs FCM token updates and push receipts.

---

## Use of AI Tools Statement

During the development of the SkillSwap Android application for the OPSC6312 Part 3 Final Portfolio of Evidence (PoE), Artificial Intelligence (AI) tools—specifically Large Language Models (LLMs)—were utilized as an intelligent collaborative pair programming assistant. The integration of AI tools played a pivotal role in accelerating development velocity, refining technical architecture, and ensuring rigorous quality assurance across both the Kotlin Android client and the Node.js/Express REST API.

AI assistance was strategically employed across four primary domain areas:

1. **Automated Unit Testing & Validation Logic**: AI was leveraged to formulate isolated unit tests for ViewModels, password hashing algorithms, and repositories (`ValidationTest`, `AuthRepositoryTest`, `PasswordUtilsTest`). By utilizing AI to generate mock response scenarios with Mockito and Kotlin Coroutine Test dispatchers (`runTest`), test coverage was systematically expanded to ensure input edge cases, malformed emails, and invalid authentication attempts were handled gracefully without application crashes.

2. **CI/CD Automation via GitHub Actions**: Formulating a robust GitHub Actions workflow requires specific YAML syntax for Android SDK environments. AI tools assisted in drafting `.github/workflows/build.yml`, configuring JDK 17 (Temurin distribution), Gradle dependency caching, execution of `./gradlew testDebugUnitTest`, and automated artifact uploading for compiled debug APKs. This ensured seamless automated build verification on every repository push and pull request.

3. **Multi-Language Localization (South African Languages)**: Achieving genuine accessibility required supporting South African languages. AI tools were utilized to generate accurate string resource translations for **isiZulu (`res/values-zu/strings.xml`)** and **Afrikaans (`res/values-af/strings.xml`)**, covering UI elements, offline sync banners, creation dialogs, and push notification descriptions.

4. **ProGuard Rules & Security Optimization**: To prepare the application for Google Play Store publication, AI was consulted to structure R8/ProGuard rules in `proguard-rules.pro`. This prevented reflection issues with Retrofit JSON serialization, Room database DAOs, and Firebase Cloud Messaging while safely shrinking release binaries.

**Human Review, Verification, and Academic Integrity**:
All code snippets, configuration files, and translations suggested by AI tools underwent strict human review, manual refactoring, and empirical verification. Every AI-assisted component was integrated following modern Android **MVVM (Model-View-ViewModel)** architecture and the **Repository Pattern**. Quality was validated through manual device execution and running automated Gradle build tasks (`./gradlew testDebugUnitTest assembleDebug`). 

Ultimately, AI tools served as an empowering efficiency multiplier. They enabled rapid prototyping of repetitive boilerplates, allowed deeper focus on core business logic—such as WorkManager offline synchronization and EncryptedSharedPreferences token security—and ensured the final submission met professional software engineering standards.

---

## References

- Android Developers (2025) *WorkManager: Asynchronous Backwards-Compatible Task Execution*. Available at: https://developer.android.com/topic/libraries/architecture/workmanager [Accessed 3 Nov 2025].
- Firebase (2025) *Firebase Cloud Messaging Documentation*. Available at: https://firebase.google.com/docs/cloud-messaging [Accessed 3 Nov 2025].
- GitHub Actions Marketplace (2025) *Automated Build Android App with GitHub Action*. Available at: https://github.com/marketplace/actions/automated-build-android-app-with-github-action [Accessed 3 Nov 2025].
- Google Identity (2025) *Google Sign-In for Android Integration Guide*. Available at: https://developers.google.com/identity/sign-in/android/start-integrating [Accessed 3 Nov 2025].
- MongoDB, Inc. (2025) *MongoDB Atlas Documentation*. Available at: https://www.mongodb.com/docs/atlas/ [Accessed 3 Nov 2025].
