package org.mccmarion.radio.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.mccmarion.radio.Config
import org.mccmarion.radio.data.ScheduleDay
import org.mccmarion.radio.data.ScheduleProgram
import org.mccmarion.radio.data.ScheduleResponse
import java.util.Calendar

class ScheduleService {

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

    private val _schedule = MutableStateFlow<List<ScheduleDay>>(emptyList())
    val schedule: StateFlow<List<ScheduleDay>> = _schedule.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val shortDayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    suspend fun loadSchedule() {
        if (_schedule.value.isNotEmpty()) return

        _isLoading.value = true
        _error.value = null

        try {
            val response: ScheduleResponse = client.get(Config.SCHEDULE_URL).body()

            val scheduleDays = response.schedule.mapIndexed { index, dayResponse ->
                val dayIndex = dayNames.indexOfFirst {
                    it.equals(dayResponse.day, ignoreCase = true)
                }.takeIf { it >= 0 } ?: index

                ScheduleDay(
                    id = dayIndex,
                    name = dayNames.getOrElse(dayIndex) { dayResponse.day },
                    shortName = shortDayNames.getOrElse(dayIndex) { dayResponse.day.take(3) },
                    programs = dayResponse.programs.mapIndexed { programIndex, program ->
                        ScheduleProgram(
                            id = "${dayIndex}_$programIndex",
                            name = program.title,
                            description = program.description,
                            startTime = program.time,
                            endTime = program.endTime
                        )
                    }
                )
            }.sortedBy { it.id }

            _schedule.value = scheduleDays
        } catch (e: Exception) {
            _error.value = "Failed to load schedule. Please try again."
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun refresh() {
        _schedule.value = emptyList()
        loadSchedule()
    }

    fun currentDayIndex(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.SUNDAY = 1
    }

    fun release() {
        client.close()
    }
}
