package br.com.mmmarq1976.mina.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mmmarq1976.mina.util.ArtworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ArtworkImage(
    fileUri: String?,
    modifier: Modifier = Modifier,
    folderUri: String? = null,
    size: Dp = 48.dp,
    fallbackIconSize: Dp = 24.dp
) {
    val context = LocalContext.current

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = fileUri, key2 = folderUri) {
        value = withContext(Dispatchers.IO) {
            var bytes: ByteArray? = null

            // 1. Try to load folder cover if folderUri is specified
            if (folderUri != null) {
                bytes = ArtworkUtils.getFolderFrontCoverBytes(context, folderUri)
            }

            // 2. Fall back to embedded track artwork if folder cover is not found
            if (bytes == null && fileUri != null) {
                bytes = ArtworkUtils.getEmbeddedPicture(context, fileUri)
            }

            if (bytes != null) {
                try {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bitmap?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imageBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music Placeholder",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(fallbackIconSize)
            )
        }
    }
}
