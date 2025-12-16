package org.mccmarion.radio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.mccmarion.radio.Config
import org.mccmarion.radio.data.ListenerInfo
import org.mccmarion.radio.data.PlaybackState
import org.mccmarion.radio.data.Track
import org.mccmarion.radio.ui.theme.Primary
import org.mccmarion.radio.ui.theme.Error

@Composable
fun PlayerScreen(
    playbackState: PlaybackState,
    currentTrack: Track?,
    albumArtUrl: String?,
    currentQuality: Config.StreamQuality,
    listeners: ListenerInfo?,
    isLive: Boolean,
    streamerName: String,
    isConnected: Boolean,
    onPlayPause: () -> Unit,
    onReconnect: () -> Unit,
    onQualityChange: (Config.StreamQuality) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Primary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Offline Banner
            if (!isConnected) {
                Spacer(modifier = Modifier.height(8.dp))
                OfflineBanner()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Album Art
            AlbumArt(imageUrl = albumArtUrl)

            Spacer(modifier = Modifier.height(24.dp))

            // Station Info
            StationInfo()

            Spacer(modifier = Modifier.height(16.dp))

            // Now Playing
            NowPlayingCard(
                track = currentTrack,
                isLoading = playbackState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Play Button
            PlayButton(
                isPlaying = playbackState.isPlaying,
                isLoading = playbackState.isLoading,
                onClick = {
                    if (playbackState.errorMessage != null) {
                        onReconnect()
                    } else {
                        onPlayPause()
                    }
                }
            )

            // Error Message
            playbackState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                ErrorCard(message = error, onReconnect = onReconnect)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quality Selector
            QualitySelector(
                currentQuality = currentQuality,
                onQualityChange = onQualityChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Bar
            StatusBar(
                isLive = isLive,
                streamerName = streamerName,
                listeners = listeners
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfflineBanner() {
    Surface(
        color = Error,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "No internet connection",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun AlbumArt(imageUrl: String?) {
    AsyncImage(
        model = imageUrl ?: Config.STATION_LOGO_URL,
        contentDescription = "Album Art",
        modifier = Modifier
            .size(280.dp)
            .shadow(20.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun StationInfo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            Config.STATION_NAME,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            Config.STATION_TAGLINE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NowPlayingCard(track: Track?, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NOW PLAYING",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = Primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Text(
                    "Loading...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (track != null) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (track.album.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            } else {
                Text(
                    "Tap play to start listening",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlayButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        enabled = !isLoading,
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onReconnect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Error.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReconnect,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reconnect")
            }
        }
    }
}

@Composable
private fun QualitySelector(
    currentQuality: Config.StreamQuality,
    onQualityChange: (Config.StreamQuality) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Stream Quality",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Config.StreamQuality.entries.forEach { quality ->
                    val isSelected = quality == currentQuality
                    Button(
                        onClick = { onQualityChange(quality) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                quality.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                quality.bitrate,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBar(
    isLive: Boolean,
    streamerName: String,
    listeners: ListenerInfo?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Live Indicator
        if (isLive) {
            Surface(
                color = Error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Error, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Error
                    )
                    if (streamerName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "with $streamerName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        // Listener Count
        listeners?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${it.current}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
