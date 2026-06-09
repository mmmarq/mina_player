package br.com.mmmarq1976.mina.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Checkbox
import br.com.mmmarq1976.mina.data.model.Track
import br.com.mmmarq1976.mina.ui.MainViewModel
import br.com.mmmarq1976.mina.ui.components.ArtworkImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tracks: List<Track>,
    albums: Map<String, List<Track>>,
    artists: Map<String, List<Track>>,
    folders: Map<String, List<Track>>,
    genres: Map<String, List<Track>>,
    searchQuery: String,
    searchResults: List<Track>,
    selectedScopes: Set<MainViewModel.SearchScope>,
    matchFullWords: Boolean,
    isScanning: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleScopeClick: (MainViewModel.SearchScope) -> Unit,
    onMatchFullWordsChange: (Boolean) -> Unit,
    onTrackClick: (Int) -> Unit,
    onSearchTrackClick: (Track) -> Unit,
    onGroupClick: (String, String) -> Unit,
    onChangeFolderClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onShuffleClick: (List<Track>) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Tracks", "Albums", "Artists", "Folders", "Genres", "Search")
    val focusManager = LocalFocusManager.current

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MINA Player",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 500) {
                            tapCount++
                        } else {
                            tapCount = 1
                        }
                        lastTapTime = currentTime
                        if (tapCount >= 3) {
                            tapCount = 0
                            onAboutClick()
                        }
                    }
            )

            IconButton(onClick = onRefreshClick) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Re-scan Folder")
            }

            IconButton(onClick = onChangeFolderClick) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Change Folder")
            }
        }

        // Scanning Status indicator
        if (isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Tab Navigation with Left/Right Arrows for Clear Scrolling Guidance
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (selectedTab > 0) selectedTab-- },
                enabled = selectedTab > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Tab"
                )
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                modifier = Modifier.weight(1f)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            IconButton(
                onClick = { if (selectedTab < tabTitles.lastIndex) selectedTab++ },
                enabled = selectedTab < tabTitles.lastIndex
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Tab"
                )
            }
        }

        // Content Area based on Tab selection
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> TracksList(tracks = tracks, onTrackClick = onTrackClick)
                1 -> AlbumsList(
                    albums = albums,
                    onItemClick = { name -> onGroupClick("Album", name) }
                )
                2 -> GroupedList(
                    groups = artists,
                    icon = Icons.Default.Person,
                    onItemClick = { name -> onGroupClick("Artist", name) }
                )
                3 -> GroupedList(
                    groups = folders,
                    icon = Icons.Default.Folder,
                    onItemClick = { name -> onGroupClick("Folder", name) }
                )
                4 -> GroupedList(
                    groups = genres,
                    icon = Icons.Default.LibraryMusic,
                    onItemClick = { name -> onGroupClick("Genre", name) }
                )
                5 -> SearchTabScreen(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    searchResults = searchResults,
                    selectedScopes = selectedScopes,
                    onToggleScope = onToggleScopeClick,
                    matchFullWords = matchFullWords,
                    onMatchFullWordsChange = onMatchFullWordsChange,
                    onTrackClick = onSearchTrackClick
                )
            }
        }
    } // Closes Column

    if (tracks.isNotEmpty()) {
        FloatingActionButton(
            onClick = {
                val listToShuffle = if (selectedTab == 5) searchResults else tracks
                if (listToShuffle.isNotEmpty()) {
                    onShuffleClick(listToShuffle)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle Play"
            )
        }
    }
} // Closes Box
} // Closes DashboardScreen Composable

@Composable
fun TracksList(
    tracks: List<Track>,
    onTrackClick: (Int) -> Unit
) {
    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No songs found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(tracks) { index, track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackClick(index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArtworkImage(
                        fileUri = track.fileUri,
                        folderUri = track.parentFolderUri,
                        size = 48.dp,
                        fallbackIconSize = 24.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.displayArtist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun GroupedList(
    groups: Map<String, List<Track>>,
    icon: ImageVector,
    onItemClick: (String) -> Unit
) {
    if (groups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No folders/tags found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(groups.keys.toList().sorted()) { name ->
                val trackCount = groups[name]?.size ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(name) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$trackCount songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun AlbumsList(
    albums: Map<String, List<Track>>,
    onItemClick: (String) -> Unit
) {
    if (albums.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No albums found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(albums.keys.toList().sorted()) { name ->
                val trackList = albums[name] ?: emptyList()
                val trackCount = trackList.size
                val firstTrack = trackList.firstOrNull()
                val parentFolderUri = firstTrack?.parentFolderUri
                val fileUri = firstTrack?.fileUri

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(name) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArtworkImage(
                        fileUri = fileUri,
                        folderUri = parentFolderUri,
                        size = 48.dp,
                        fallbackIconSize = 24.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$trackCount songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun SearchTabScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<Track>,
    selectedScopes: Set<MainViewModel.SearchScope>,
    onToggleScope: (MainViewModel.SearchScope) -> Unit,
    matchFullWords: Boolean,
    onMatchFullWordsChange: (Boolean) -> Unit,
    onTrackClick: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Query Box
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search your music library...") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Title for search options
        Text(
            text = "Search Options",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Match options (full words checkbox)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMatchFullWordsChange(!matchFullWords) }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = matchFullWords,
                onCheckedChange = onMatchFullWordsChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Match full words",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (matchFullWords) "Matches exact words only" else "Matches any partial text sequence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Multi-selection categories for search scope
        Text(
            text = "Filter Scopes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Horizontal scrolling checkboxes for scopes
        val scopesList = listOf(
            MainViewModel.SearchScope.TRACK to "Track",
            MainViewModel.SearchScope.ALBUM to "Album",
            MainViewModel.SearchScope.ARTIST to "Artist",
            MainViewModel.SearchScope.FOLDER to "Folder",
            MainViewModel.SearchScope.GENRE to "Genre"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            scopesList.forEach { (scope, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleScope(scope) }
                ) {
                    Checkbox(
                        checked = selectedScopes.contains(scope),
                        onCheckedChange = { onToggleScope(scope) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Divider(modifier = Modifier.padding(top = 12.dp))

        // Search Results List
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (query.isBlank()) "Type to search your music library" else "No results found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(searchResults) { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackClick(track) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkImage(
                            fileUri = track.fileUri,
                            folderUri = track.parentFolderUri,
                            size = 48.dp,
                            fallbackIconSize = 24.dp
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${track.displayArtist} • ${track.displayAlbum}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
