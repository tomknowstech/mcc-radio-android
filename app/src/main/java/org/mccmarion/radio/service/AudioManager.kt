package org.mccmarion.radio.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mccmarion.radio.Config
import org.mccmarion.radio.data.PlaybackState
import org.mccmarion.radio.data.Track

class AudioManager(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentQuality = MutableStateFlow(Config.StreamQuality.HIFI)
    val currentQuality: StateFlow<Config.StreamQuality> = _currentQuality.asStateFlow()

    private var currentTrack: Track? = null

    fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                updatePlaybackState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isLoading = false,
                    errorMessage = "Playback error: ${error.message}"
                )
            }
        })
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        _playbackState.value = PlaybackState(
            isPlaying = controller.isPlaying,
            isLoading = controller.playbackState == Player.STATE_BUFFERING,
            errorMessage = null
        )
    }

    fun play() {
        val controller = mediaController ?: return

        _playbackState.value = _playbackState.value.copy(isLoading = true, errorMessage = null)

        val mediaItem = PlaybackService.createMediaItem(
            streamUrl = _currentQuality.value.url,
            title = currentTrack?.title ?: Config.STATION_NAME,
            artist = currentTrack?.artist ?: Config.STATION_TAGLINE,
            artworkUri = currentTrack?.artUrl ?: Config.STATION_LOGO_URL
        )

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun setQuality(quality: Config.StreamQuality) {
        val wasPlaying = mediaController?.isPlaying == true
        _currentQuality.value = quality
        if (wasPlaying) {
            play()
        }
    }

    fun updateMetadata(track: Track) {
        currentTrack = track
        val controller = mediaController ?: return

        if (controller.isPlaying) {
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.artUrl?.let { android.net.Uri.parse(it) })
                .build()

            val currentItem = controller.currentMediaItem
            if (currentItem != null) {
                val updatedItem = currentItem.buildUpon()
                    .setMediaMetadata(metadata)
                    .build()
                controller.replaceMediaItem(0, updatedItem)
            }
        }
    }

    fun reconnect() {
        _playbackState.value = PlaybackState()
        play()
    }

    fun release() {
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}
