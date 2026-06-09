package br.com.mmmarq1976.mina.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.mmmarq1976.mina.data.model.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("""
        SELECT * FROM tracks 
        WHERE title LIKE :query 
           OR fileName LIKE :query 
           OR album LIKE :query 
           OR artist LIKE :query 
           OR genre LIKE :query
        ORDER BY CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END ASC
    """)
    fun searchTracks(query: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE fileUri = :uri LIMIT 1")
    suspend fun getTrackByUri(uri: String): Track?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Query("DELETE FROM tracks")
    suspend fun clearAllTracks()
}
