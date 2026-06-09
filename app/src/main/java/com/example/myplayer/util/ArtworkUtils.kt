package br.com.mmmarq1976.mina.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object ArtworkUtils {
    fun getEmbeddedPicture(context: Context, uriString: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
                retriever.embeddedPicture
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun getFolderFrontCoverBytes(context: Context, folderUriString: String): ByteArray? {
        return try {
            val folderUri = Uri.parse(folderUriString)
            val dir = DocumentFile.fromTreeUri(context, folderUri)
            if (dir != null && dir.exists() && dir.isDirectory) {
                val files = dir.listFiles()
                val frontFile = files.find { file ->
                    file.name?.endsWith("front.jpg", ignoreCase = true) == true
                }
                if (frontFile != null) {
                    context.contentResolver.openInputStream(frontFile.uri)?.use { inputStream ->
                        inputStream.readBytes()
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
