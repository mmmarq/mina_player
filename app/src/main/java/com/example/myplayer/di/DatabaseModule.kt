package br.com.mmmarq1976.mina.di

import android.content.Context
import androidx.room.Room
import br.com.mmmarq1976.mina.data.local.AppDatabase
import br.com.mmmarq1976.mina.data.local.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "myplayer_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }
}
