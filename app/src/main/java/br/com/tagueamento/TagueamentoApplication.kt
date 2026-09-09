package br.com.tagueamento

import android.app.Application
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel

class TagueamentoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val appToken = "ggg8gp5dgt8i"
        val environment = AdjustConfig.ENVIRONMENT_SANDBOX

        val config = AdjustConfig(this, appToken, environment)
        config.setLogLevel(LogLevel.VERBOSE)

        Adjust.initSdk(config)
    }
}
