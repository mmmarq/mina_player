package br.com.mmmarq1976.mina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mmmarq1976.mina.data.model.Track
import br.com.mmmarq1976.mina.data.repository.SettingsRepository
import br.com.mmmarq1976.mina.data.repository.TrackRepository
import br.com.mmmarq1976.mina.playback.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val settingsRepository: SettingsRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    val musicFolderUri = settingsRepository.musicFolderUri
    val isScanning = settingsRepository.isScanning

    enum class SearchScope {
        TRACK, ALBUM, ARTIST, FOLDER, GENRE
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedScopes = MutableStateFlow(setOf(SearchScope.TRACK, SearchScope.ALBUM, SearchScope.ARTIST))
    val selectedScopes = _selectedScopes.asStateFlow()

    private val _matchFullWords = MutableStateFlow(false)
    val matchFullWords = _matchFullWords.asStateFlow()

    // Expose playback states directly to UI
    val currentTrack = musicServiceConnection.currentTrack
    val isPlaying = musicServiceConnection.isPlaying
    val currentPosition = musicServiceConnection.currentPosition
    val duration = musicServiceConnection.duration

    // Tracks represents the full scanned media library
    val tracks: StateFlow<List<Track>> = trackRepository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // searchResults dynamically filters the full tracks list based on query, scopes, and full-word matching
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Track>> = combine(
        tracks,
        _searchQuery,
        _selectedScopes,
        _matchFullWords
    ) { allTracks, query, scopes, fullWords ->
        if (query.isBlank()) {
            return@combine emptyList<Track>()
        }

        val cleanQuery = query.trim()
        val regex = if (fullWords) {
            "\\b${Regex.escape(cleanQuery)}\\b".toRegex(RegexOption.IGNORE_CASE)
        } else {
            null
        }

        allTracks.filter { track ->
            val activeScopes = if (scopes.isEmpty()) SearchScope.values().toSet() else scopes
            activeScopes.any { scope ->
                val fieldValue = when (scope) {
                    SearchScope.TRACK -> track.displayName
                    SearchScope.ALBUM -> track.displayAlbum
                    SearchScope.ARTIST -> track.displayArtist
                    SearchScope.FOLDER -> track.parentFolderName
                    SearchScope.GENRE -> track.displayGenre
                }

                if (fieldValue.isNullOrBlank()) {
                    false
                } else if (regex != null) {
                    regex.containsMatchIn(fieldValue)
                } else {
                    fieldValue.contains(cleanQuery, ignoreCase = true)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Grouping by Album (sorted by track number)
    val albums: StateFlow<Map<String, List<Track>>> = tracks
        .map { list ->
            list.groupBy { it.displayAlbum }
                .mapValues { entry ->
                    entry.value.sortedWith(compareBy { it.trackNumber ?: 0 })
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Grouping by Artist (sorted by Album Name and Track Number)
    val artists: StateFlow<Map<String, List<Track>>> = tracks
        .map { list ->
            list.groupBy { it.displayArtist }
                .mapValues { entry ->
                    entry.value.sortedWith(compareBy({ it.displayAlbum }, { it.trackNumber ?: 0 }))
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Grouping by Folder
    val folders: StateFlow<Map<String, List<Track>>> = tracks
        .map { list ->
            list.groupBy { it.parentFolderName }
                .mapValues { entry ->
                    entry.value.sortedWith(compareBy { it.displayName })
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Grouping by Genre
    val genres: StateFlow<Map<String, List<Track>>> = tracks
        .map { list ->
            list.groupBy { it.displayGenre }
                .mapValues { entry ->
                    entry.value.sortedWith(compareBy { it.displayName })
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearchScope(scope: SearchScope) {
        val current = _selectedScopes.value
        _selectedScopes.value = if (current.contains(scope)) {
            current - scope
        } else {
            current + scope
        }
    }

    fun setMatchFullWords(match: Boolean) {
        _matchFullWords.value = match
    }

    fun setMusicFolder(uri: String?) {
        settingsRepository.saveMusicFolderUri(uri)
        if (uri != null) {
            triggerScan()
        }
    }

    fun triggerScan() {
        viewModelScope.launch {
            trackRepository.scanSelectedFolder()
        }
    }

    // Playback delegation
    fun play() = musicServiceConnection.play()
    fun pause() = musicServiceConnection.pause()
    fun next() = musicServiceConnection.next()
    fun previous() = musicServiceConnection.previous()
    fun seekTo(position: Long) = musicServiceConnection.seekTo(position)

    fun playTracks(trackList: List<Track>, startIndex: Int = 0) {
        if (trackList.isNotEmpty()) {
            musicServiceConnection.playTrackList(trackList, startIndex)
        }
    }
}
