package br.com.mmmarq1976.mina.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import br.com.mmmarq1976.mina.data.model.Track
import br.com.mmmarq1976.mina.data.repository.TrackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackRepository: TrackRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var progressUpdateJob: Job? = null

    init {
        scope.launch {
            try {
                val sessionToken = SessionToken(
                    context,
                    ComponentName(context, PlaybackService::class.java)
                )
                mediaController = MediaController.Builder(context, sessionToken)
                    .buildAsync()
                    .await()

                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (isPlaying) {
                            startProgressUpdate()
                        } else {
                            stopProgressUpdate()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        updateCurrentTrack(mediaItem)
                        mediaItem?.mediaId?.let { uri ->
                            scope.launch {
                                trackRepository.incrementPlayCount(uri)
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            _duration.value = mediaController?.duration ?: 0L
                        }
                    }
                })

                // Initialize state if service is already running
                mediaController?.let { controller ->
                    _isPlaying.value = controller.isPlaying
                    _duration.value = controller.duration.coerceAtLeast(0L)
                    updateCurrentTrack(controller.currentMediaItem)
                    if (controller.isPlaying) {
                        startProgressUpdate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateCurrentTrack(mediaItem: MediaItem?) {
        val uri = mediaItem?.mediaId ?: return
        scope.launch {
            val track = trackRepository.getTrackByUri(uri)
            _currentTrack.value = track
            if (track != null) {
                _duration.value = track.duration
            }
        }
    }

    fun refreshCurrentTrack() {
        val currentMediaItem = mediaController?.currentMediaItem
        updateCurrentTrack(currentMediaItem)
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressUpdateJob = scope.launch {
            while (true) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun previous() {
        mediaController?.seekToPrevious()
    }

    fun playTrackList(tracks: List<Track>, startIndex: Int = 0) {
        val controller = mediaController ?: return
        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.fileUri)
                .setUri(Uri.parse(track.fileUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.displayName)
                        .setArtist(track.displayArtist)
                        .setAlbumTitle(track.displayAlbum)
                        .setGenre(track.displayGenre)
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }
}
