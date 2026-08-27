package com.mobin.app

import android.app.Application
import com.mobin.app.util.DataStoreManager

class MobInApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataStoreManager.init(this)
    }
}
