package br.com.mmmarq1976.mina.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.mmmarq1976.mina.data.model.Track

@Database(entities = [Track::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
