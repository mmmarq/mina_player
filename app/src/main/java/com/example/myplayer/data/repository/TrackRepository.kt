package br.com.mmmarq1976.mina.data.repository

import br.com.mmmarq1976.mina.data.local.TrackDao
import br.com.mmmarq1976.mina.data.model.Track
import br.com.mmmarq1976.mina.data.scanner.MusicScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val musicScanner: MusicScanner,
    private val settingsRepository: SettingsRepository
) {

    fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks()
    }

    fun searchTracks(query: String): Flow<List<Track>> {
        if (query.isBlank()) return trackDao.getAllTracks()
        return trackDao.searchTracks("%$query%")
    }

    suspend fun getTrackByUri(uri: String): Track? {
        return trackDao.getTrackByUri(uri)
    }

    suspend fun incrementPlayCount(uri: String) {
        trackDao.incrementPlayCount(uri)
    }

    suspend fun scanSelectedFolder() {
        val folderUri = settingsRepository.musicFolderUri.value ?: return
        settingsRepository.setScanning(true)
        try {
            val scannedTracks = musicScanner.scanFolder(folderUri)
            trackDao.clearAllTracks()
            if (scannedTracks.isNotEmpty()) {
                trackDao.insertTracks(scannedTracks)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            settingsRepository.setScanning(false)
        }
    }
}
