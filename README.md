# MINA Player - Android MP3 Player

A high-fidelity, polished, and fully reactive local MP3 file player for Android built with Kotlin, **Jetpack Compose (Material 3)**, and **AndroidX Media3 (ExoPlayer & MediaSession)**.

The entire application has been designed from the ground up as a high-performance **offline audio player**. It completely bypasses resource-heavy internet streaming layers to preserve battery life, memory, and give you immediate, native control of your offline music library.

---

## Core Features

*   **SAF Folder Picker:** Select any local folder on your device. The app recursively scans it for MP3 files and persists read permissions across app launches and device restarts.
*   **Background Scanning & Caching:** Extracts audio metadata (Title, Album, Artist, Genre, Track Number) using `MediaMetadataRetriever` in a background coroutine and caches them inside a local **Room Database** (Version 2) for high-performance querying and grouping.
*   **Smart Music Browsing categories (6 Tabs):**
    *   **Tracks:** Alphabetical list of all songs in the library.
    *   **Albums:** Grouped by album name, sorted by track number, with album cover support.
    *   **Artists:** Grouped by artist name, sorted by album name and track number.
    *   **Folders:** Browse files grouped by their physical subdirectories.
    *   **Genres:** Browse files grouped by their musical genres.
    *   **Search:** Dedicated search suite with multi-selection filters.
*   **Chevron Tab Navigation:** Interactive left and right arrow indicators sit on either side of the category row, providing clear scroll guidance and a handy tap interface to quickly slide through panels.
*   **Advanced Scoped Search Engine:** Located in its own dedicated "Search" tab, this tool features:
    *   **Filter Scopes:** Checkboxes to restrict search queries to any combination of **Track Name**, **Album Name**, **Artist**, **Folder**, or **Genre**.
    *   **Match Full Words Checkbox:** Toggles between standard partial string sequence matching (substring match) and exact whole word matching bounded by word boundaries (`\b` via native Regex compilation).
*   **Contextual Random Play (Shuffle):** A floating action button on the dashboard enables one-tap random playback. It is fully **context-sensitive**:
    *   On standard tabs, tapping it shuffles and plays your **entire music library**.
    *   On the Search tab, it shuffles and plays **only the filtered search results** matching your active query and scopes!
*   **Folder Cover Art Integration:** Automatically checks the containing folder of a song for any image ending with `front.jpg` (case-insensitive, e.g. `front.jpg`, `album_front.jpg`) to display as the album cover before falling back to embedded MP3 ID3 tag picture extraction.
*   **Rich Playback Engine:** Powered by AndroidX Media3 `MediaSessionService` running as a foreground service with background notification controls and automatic system audio focus handling.
*   **Mini Player & Now Playing Screen:**
    *   A persistent bottom mini player lets you browse other tabs while music continues playing.
    *   Now Playing view displays high-fidelity album art (folder-based or embedded), song details, next, previous, play/pause controls, and an interactive seek slider.

---

## Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Media Playback:** AndroidX Media3 (`media3-exoplayer`, `media3-session`, `media3-common`)
*   **Local Caching & Persistence:** Room Database (Version 2 with fallbacks)
*   **Dependency Injection:** Dagger Hilt
*   **Concurrency:** Kotlin Coroutines & StateFlow (reactive MVVM structure)
*   **Image Loading:** Custom high-fidelity asynchronous byte-array bitmap renderer for local embedded MP3 art & folder covers

---

## Getting Started

### 1. Project Setup
1. Open **Android Studio** (Koala or newer recommended).
2. Choose **File -> Open** and select this directory (`myplayer`).
3. Android Studio will automatically initialize the Gradle wrapper and download the required SDK/dependencies.

### 2. Configure Keystore Signing (Optional for Release Builds)
To sign release APKs cleanly without committing credentials to source control, configure your local uncommitted `local.properties` file:
```properties
signing.store.file=/absolute/path/to/your/my-release-key.keystore
signing.store.password=YOUR_KEYSTORE_PASSWORD
signing.key.alias=YOUR_KEY_ALIAS
signing.key.password=YOUR_KEY_PASSWORD
```
If these properties are not specified locally, the build system safely falls back to compiling release variants with your local Android SDK debug certificate.

### 3. Compilation Commands
*   **Compile Debug App:**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Compile Signed Release App:**
    ```bash
    ./gradlew assembleRelease
    ```
*   **Run Unit Tests:**
    ```bash
    ./gradlew test
    ```
