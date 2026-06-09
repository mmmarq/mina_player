package br.com.mmmarq1976.mina.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import br.com.mmmarq1976.mina.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun scanFolder(folderUriString: String): List<Track> = withContext(Dispatchers.IO) {
        val tracksList = mutableListOf<Track>()
        try {
            val rootUri = Uri.parse(folderUriString)
            // Persist read permission for future app launches
            try {
                context.contentResolver.takePersistableUriPermission(
                    rootUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission might have already been persisted, ignore
            }

            val rootDir = DocumentFile.fromTreeUri(context, rootUri)
            if (rootDir != null && rootDir.exists() && rootDir.isDirectory) {
                scanDirectory(rootDir, rootDir.uri.toString(), rootDir.name ?: "Root Folder", tracksList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext tracksList
    }

    private fun scanDirectory(
        directory: DocumentFile,
        parentUriString: String,
        parentName: String,
        result: MutableList<Track>
    ) {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanDirectory(file, file.uri.toString(), file.name ?: "Unknown Folder", result)
            } else if (file.isFile && file.name?.endsWith(".mp3", ignoreCase = true) == true) {
                val track = extractTrackInfo(file, parentUriString, parentName)
                if (track != null) {
                    result.add(track)
                }
            }
        }
    }

    private fun extractTrackInfo(
        file: DocumentFile,
        parentUriString: String,
        parentName: String
    ): Track? {
        val retriever = MediaMetadataRetriever()
        try {
            context.contentResolver.openFileDescriptor(file.uri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L
                val trackNumberStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)

                return Track(
                    fileUri = file.uri.toString(),
                    title = title?.trim(),
                    fileName = file.name ?: "Unknown File",
                    album = album?.trim(),
                    artist = artist?.trim(),
                    genre = genre?.trim(),
                    trackNumber = parseTrackNumber(trackNumberStr),
                    duration = duration,
                    parentFolderUri = parentUriString,
                    parentFolderName = parentName
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
        return null
    }

    private fun parseTrackNumber(trackNumberStr: String?): Int? {
        if (trackNumberStr.isNullOrBlank()) return null
        return try {
            val cleanStr = trackNumberStr.split("/")[0].trim()
            cleanStr.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
