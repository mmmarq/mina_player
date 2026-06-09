package br.com.mmmarq1976.mina.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val fileUri: String,
    val title: String?,
    val fileName: String,
    val album: String?,
    val artist: String?,
    val genre: String?,
    val trackNumber: Int?,
    val duration: Long,
    val parentFolderUri: String,
    val parentFolderName: String
) {
    val displayName: String
        get() = if (title.isNullOrBlank()) fileName else title

    val displayArtist: String
        get() = if (artist.isNullOrBlank()) "Unknown Artist" else artist

    val displayAlbum: String
        get() = if (album.isNullOrBlank()) "Unknown Album" else album

    val displayGenre: String
        get() = if (genre.isNullOrBlank()) "Unknown Genre" else genre
}
