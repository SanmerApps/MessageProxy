package app.sanmer.message.proxy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    init {
        Timber.plant(Timber.DebugTree())
    }
}