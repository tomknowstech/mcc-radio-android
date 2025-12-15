package org.mccmarion.radio.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Now Playing API Response
@Serializable
data class NowPlayingResponse(
    @SerialName("now_playing") val nowPlaying: NowPlayingInfo? = null,
    val listeners: ListenerInfo? = null,
    val live: LiveInfo? = null,
    @SerialName("station") val station: StationInfo? = null
)

@Serializable
data class NowPlayingInfo(
    val song: SongInfo? = null
)

@Serializable
data class SongInfo(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val art: String? = null
)

@Serializable
data class ListenerInfo(
    val current: Int = 0,
    val total: Int = 0,
    val unique: Int = 0
)

@Serializable
data class LiveInfo(
    @SerialName("is_live") val isLive: Boolean = false,
    @SerialName("streamer_name") val streamerName: String = ""
)

@Serializable
data class StationInfo(
    val name: String = ""
)

// Track model for UI
data class Track(
    val title: String,
    val artist: String,
    val album: String,
    val artUrl: String?
)

// Schedule models
@Serializable
data class ScheduleResponse(
    val schedule: List<ScheduleDayResponse> = emptyList()
)

@Serializable
data class ScheduleDayResponse(
    val day: String,
    val programs: List<ScheduleProgramResponse> = emptyList()
)

@Serializable
data class ScheduleProgramResponse(
    val name: String,
    val description: String = "",
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String
)

// UI Models
data class ScheduleDay(
    val id: Int,
    val name: String,
    val shortName: String,
    val programs: List<ScheduleProgram>
)

data class ScheduleProgram(
    val id: String,
    val name: String,
    val description: String,
    val startTime: String,
    val endTime: String
) {
    val timeRange: String get() = "$startTime - $endTime"

    fun isCurrentlyPlaying(): Boolean {
        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(java.util.Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMinute

        val startMinutes = parseTimeToMinutes(startTime)
        val endMinutes = parseTimeToMinutes(endTime)

        return if (endMinutes > startMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            // Handles overnight programs
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.replace(" AM", "").replace(" PM", "").split(":")
        var hour = parts[0].toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        if (time.contains("PM") && hour != 12) hour += 12
        if (time.contains("AM") && hour == 12) hour = 0

        return hour * 60 + minute
    }
}

// Playback state
data class PlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
