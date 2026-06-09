package br.com.mmmarq1976.mina.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("myplayer_prefs", Context.MODE_PRIVATE)

    private val _musicFolderUri = MutableStateFlow(prefs.getString(KEY_MUSIC_FOLDER_URI, null))
    val musicFolderUri: StateFlow<String?> = _musicFolderUri

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun saveMusicFolderUri(uri: String?) {
        prefs.edit().putString(KEY_MUSIC_FOLDER_URI, uri).apply()
        _musicFolderUri.value = uri
    }

    fun setScanning(scanning: Boolean) {
        _isScanning.value = scanning
    }

    companion object {
        private const val KEY_MUSIC_FOLDER_URI = "music_folder_uri"
    }
}
