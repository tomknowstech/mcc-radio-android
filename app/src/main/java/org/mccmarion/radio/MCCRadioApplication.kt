package org.mccmarion.radio

import android.app.Application
import org.mccmarion.radio.service.AudioManager
import org.mccmarion.radio.service.MetadataService
import org.mccmarion.radio.service.ScheduleService

class MCCRadioApplication : Application() {

    lateinit var audioManager: AudioManager
        private set

    lateinit var metadataService: MetadataService
        private set

    lateinit var scheduleService: ScheduleService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        audioManager = AudioManager(this)
        metadataService = MetadataService()
        scheduleService = ScheduleService()

        audioManager.initialize()
    }

    override fun onTerminate() {
        super.onTerminate()
        audioManager.release()
        metadataService.release()
        scheduleService.release()
    }

    companion object {
        lateinit var instance: MCCRadioApplication
            private set
    }
}
