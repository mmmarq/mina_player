package br.com.mmmarq1976.mina

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.mmmarq1976.mina.ui.MainViewModel
import br.com.mmmarq1976.mina.ui.components.MiniPlayer
import br.com.mmmarq1976.mina.ui.screen.AboutScreen
import br.com.mmmarq1976.mina.ui.screen.DashboardScreen
import br.com.mmmarq1976.mina.ui.screen.GroupDetailScreen
import br.com.mmmarq1976.mina.ui.screen.NowPlayingScreen
import br.com.mmmarq1976.mina.ui.screen.PickFolderScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Activity result launcher to change folder path
    private val changeFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Persist the read permission for restarts
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.setMusicFolder(uri.toString())
        }
    }

    // Permission launcher for API 33+ notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle outcome if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request post notification permission on API 33+ for Media3 foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val folderUri by viewModel.musicFolderUri.collectAsState()

                    if (folderUri.isNullOrBlank()) {
                        PickFolderScreen(
                            onFolderSelected = { uri ->
                                try {
                                    contentResolver.takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                viewModel.setMusicFolder(uri.toString())
                            }
                        )
                    } else {
                        AppNavigation(
                            viewModel = viewModel,
                            onChangeFolder = { changeFolderLauncher.launch(null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    onChangeFolder: () -> Unit
) {
    val navController = rememberNavController()

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val tracks by viewModel.tracks.collectAsState()
            val albums by viewModel.albums.collectAsState()
            val artists by viewModel.artists.collectAsState()
            val folders by viewModel.folders.collectAsState()
            val genres by viewModel.genres.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val searchResults by viewModel.searchResults.collectAsState()
            val selectedScopes by viewModel.selectedScopes.collectAsState()
            val matchFullWords by viewModel.matchFullWords.collectAsState()
            val isScanning by viewModel.isScanning.collectAsState()

            Scaffold(
                bottomBar = {
                    if (currentTrack != null) {
                        MiniPlayer(
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            onPlayPauseClick = {
                                if (isPlaying) viewModel.pause() else viewModel.play()
                            },
                            onNextClick = { viewModel.next() },
                            onClick = { navController.navigate("now_playing") }
                        )
                    }
                }
            ) { innerPadding ->
                DashboardScreen(
                    tracks = tracks,
                    albums = albums,
                    artists = artists,
                    folders = folders,
                    genres = genres,
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    selectedScopes = selectedScopes,
                    matchFullWords = matchFullWords,
                    isScanning = isScanning,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onToggleScopeClick = { viewModel.toggleSearchScope(it) },
                    onMatchFullWordsChange = { viewModel.setMatchFullWords(it) },
                    onTrackClick = { index ->
                        viewModel.playTracks(tracks, index)
                    },
                    onSearchTrackClick = { track ->
                        val index = searchResults.indexOf(track)
                        if (index >= 0) {
                            viewModel.playTracks(searchResults, index)
                        }
                    },
                    onGroupClick = { type, name ->
                        navController.navigate("group_detail/$type/$name")
                    },
                    onChangeFolderClick = onChangeFolder,
                    onRefreshClick = { viewModel.triggerScan() },
                    onShuffleClick = { listToShuffle ->
                        if (listToShuffle.isNotEmpty()) {
                            viewModel.playTracks(listToShuffle.shuffled(), 0)
                        }
                    },
                    onAboutClick = { navController.navigate("about") },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable("about") {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }

        composable("now_playing") {
            NowPlayingScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPauseClick = {
                    if (isPlaying) viewModel.pause() else viewModel.play()
                },
                onNextClick = { viewModel.next() },
                onPreviousClick = { viewModel.previous() },
                onSeek = { position -> viewModel.seekTo(position) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "group_detail/{groupType}/{groupName}",
            arguments = listOf(
                navArgument("groupType") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupType = backStackEntry.arguments?.getString("groupType") ?: ""
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""

            // Gather correct tracks from Hilt ViewModel state
            val groupTracks = when (groupType) {
                "Album" -> viewModel.albums.collectAsState().value[groupName] ?: emptyList()
                "Artist" -> viewModel.artists.collectAsState().value[groupName] ?: emptyList()
                "Folder" -> viewModel.folders.collectAsState().value[groupName] ?: emptyList()
                "Genre" -> viewModel.genres.collectAsState().value[groupName] ?: emptyList()
                else -> emptyList()
            }

            Scaffold(
                bottomBar = {
                    if (currentTrack != null) {
                        MiniPlayer(
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            onPlayPauseClick = {
                                if (isPlaying) viewModel.pause() else viewModel.play()
                            },
                            onNextClick = { viewModel.next() },
                            onClick = { navController.navigate("now_playing") }
                        )
                    }
                }
            ) { innerPadding ->
                GroupDetailScreen(
                    title = groupName,
                    tracks = groupTracks,
                    onTrackClick = { index ->
                        viewModel.playTracks(groupTracks, index)
                    },
                    onPlayAllClick = {
                        viewModel.playTracks(groupTracks, 0)
                    },
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
