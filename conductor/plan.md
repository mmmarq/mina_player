# MP3 Player Application Plan

## 1. Objective
Build a native Android MP3 player using Kotlin, Jetpack Compose, and AndroidX Media3. The application will allow users to pick a specific folder using the Storage Access Framework (SAF), scan it for MP3 files, cache their metadata locally, and provide a rich media playback experience.

## 2. Architecture & Tech Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Media Playback:** AndroidX Media3 (`MediaSessionService` & `ExoPlayer`)
*   **Metadata Extraction:** `MediaMetadataRetriever`
*   **Local Storage/Caching:** Room Database (to ensure fast loading of Tracks, Albums, Artists, and Genres)
*   **State Management:** Kotlin Coroutines & StateFlow (MVVM Architecture)
*   **Dependency Injection:** Hilt (Dagger)

## 3. Implementation Steps

### Phase 1: Setup & Core Infrastructure
*   Initialize an Android Studio project with Jetpack Compose.
*   Setup Room Database with entities for `Track`, `Album`, `Artist`, `Genre`, and `Folder`.
*   Configure Hilt for dependency injection.

### Phase 2: Folder Selection & Scanning (SAF)
*   Implement the UI for the initial onboarding screen to request folder access via `ACTION_OPEN_DOCUMENT_TREE`.
*   Create a background scanning service/worker to recursively traverse the selected folder using `DocumentFile`.
*   Use `MediaMetadataRetriever` to parse ID3 tags (Embedded Image, Song Name, Track Number, Artist, Album, Genre) and populate the Room Database.

### Phase 3: Media3 Playback Service
*   Implement a foreground `MediaSessionService` using ExoPlayer.
*   Manage audio focus, media session callbacks, and background playback notifications.
*   Create a ViewModel to expose the current playback state (playing, paused, current track, progress) to the UI.

### Phase 4: UI Development (Jetpack Compose)
*   **Main Dashboard:** A tabbed layout or Navigation Drawer containing:
    *   **Tracks:** Alphabetical list of all songs.
    *   **Albums:** Grid/List of albums. Clicking one shows its tracks, ordered by track number.
    *   **Artists:** List of artists. Clicking one shows their tracks, ordered by album and track number.
    *   **Folders:** Browse subfolders under the root selected folder.
    *   **Genres:** List of genres. Clicking one shows its tracks.
*   **Search:** A global search bar at the top filtering the Room Database across all metadata fields.
*   **Mini Player:** A persistent bottom bar showing the currently playing track with basic controls, clicking it opens the Now Playing screen.
*   **Now Playing Screen:** Displays the embedded MP3 image, song name (fallback to file name), track number, artist name, and provides Play/Pause, Next, Previous, and a Back button to return to the previous screen.

### Phase 5: Tag-Based Playback & Polish
*   Implement logic so clicking an Album, Artist, or Genre creates a playlist in ExoPlayer containing all matching tracks.
*   Add polished UI transitions, error handling (e.g., empty folder), and configuration options to re-scan or change the root folder.

## 4. Verification & Testing
*   Verify that `ACTION_OPEN_DOCUMENT_TREE` correctly persists permissions across app restarts.
*   Ensure the background playback service survives screen rotation and app minimization.
*   Test scanning performance with a mock directory of various MP3 files (some with missing tags to verify fallbacks).
