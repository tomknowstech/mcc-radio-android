package org.mccmarion.radio.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.mccmarion.radio.Config
import org.mccmarion.radio.data.ListenerInfo
import org.mccmarion.radio.data.NowPlayingResponse
import org.mccmarion.radio.data.Track

class MetadataService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = Config.API_TIMEOUT_MS
            connectTimeoutMillis = Config.API_TIMEOUT_MS
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _albumArtUrl = MutableStateFlow<String?>(null)
    val albumArtUrl: StateFlow<String?> = _albumArtUrl.asStateFlow()

    private val _listeners = MutableStateFlow<ListenerInfo?>(null)
    val listeners: StateFlow<ListenerInfo?> = _listeners.asStateFlow()

    private val _isLive = MutableStateFlow(false)
    val isLive: StateFlow<Boolean> = _isLive.asStateFlow()

    private val _streamerName = MutableStateFlow("")
    val streamerName: StateFlow<String> = _streamerName.asStateFlow()

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                fetchMetadata()
                delay(Config.METADATA_POLLING_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun fetchMetadata() {
        try {
            val response: NowPlayingResponse = client.get(Config.METADATA_URL).body()

            response.nowPlaying?.song?.let { song ->
                val track = Track(
                    title = song.title.ifEmpty { Config.STATION_NAME },
                    artist = song.artist.ifEmpty { "Unknown Artist" },
                    album = song.album,
                    artUrl = song.art?.takeIf { it.isNotEmpty() } ?: Config.DEFAULT_ALBUM_ART_URL
                )
                _currentTrack.value = track
                _albumArtUrl.value = track.artUrl
            }

            _listeners.value = response.listeners
            _isLive.value = response.live?.isLive ?: false
            _streamerName.value = response.live?.streamerName ?: ""

        } catch (e: Exception) {
            // Silently fail - will retry on next poll
            e.printStackTrace()
        }
    }

    fun shareText(): String {
        val track = _currentTrack.value
        return if (track != null) {
            "Listening to ${track.title} by ${track.artist} on ${Config.STATION_NAME}! ${Config.RADIO_WEB_URL}"
        } else {
            "Listening to ${Config.STATION_NAME}! ${Config.RADIO_WEB_URL}"
        }
    }

    fun release() {
        stopPolling()
        scope.cancel()
        client.close()
    }
}
