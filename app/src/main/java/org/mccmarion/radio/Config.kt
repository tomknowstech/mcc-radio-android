package org.mccmarion.radio

object Config {
    // Station Info
    const val STATION_NAME = "MCC Radio"
    const val STATION_FULL_NAME = "My Community Church Radio"
    const val STATION_TAGLINE = "Your Home for the Best in Southern Gospel"

    // Contact Info
    const val CONTACT_EMAIL = "contact@mccmarion.org"
    const val CONTACT_PHONE = "(740) 777-9622"
    const val WEBSITE_URL = "https://mccmarion.org"
    const val RADIO_WEB_URL = "https://mccmarion.org/radio_live.html"

    // Social Media
    const val FACEBOOK_URL = "https://facebook.com/MCCRadioNet"
    const val YOUTUBE_URL = "https://youtube.com/@MCCRadio"

    // Donate
    const val DONATE_URL = "https://mccmarion.org/donate.html"

    // About Text
    const val ABOUT_TEXT = """MCC Radio is the streaming voice of My Community Church, bringing you the best in Southern Gospel music 24/7.

We are currently streaming online while we prepare to launch our LPFM radio station, which we plan to build in the next year.

Our mission is to spread the Gospel through uplifting music and inspiring content."""

    // API Endpoints
    const val METADATA_URL = "https://radio.mccmarion.org/api/nowplaying/mcc_radio"
    const val SCHEDULE_URL = "https://mccmarion.org/schedule.json"
    const val METADATA_POLLING_INTERVAL_MS = 15000L
    const val API_TIMEOUT_MS = 10000L

    // Station Logo
    const val STATION_LOGO_URL = "https://radio.mccmarion.org/static/uploads/album_art.1732477227.jpg"
    const val DEFAULT_ALBUM_ART_URL = "https://radio.mccmarion.org/static/uploads/album_art.1732477227.jpg"

    // Stream Quality Options
    enum class StreamQuality(val displayName: String, val bitrate: String, val url: String) {
        HIFI("HiFi", "352k", "https://radio.mccmarion.org/hls/mcc_radio/aac_hifi.m3u8"),
        MIDFI("MidFi", "211k", "https://radio.mccmarion.org/hls/mcc_radio/aac_midfi.m3u8"),
        LOFI("LoFi", "141k", "https://radio.mccmarion.org/hls/mcc_radio/aac_lofi.m3u8")
    }
}
