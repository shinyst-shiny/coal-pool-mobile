package com.shinyst.coalpoolmobile

import android.app.Application
import com.shinyst.coalpoolmobile.data.AppContainer
import com.shinyst.coalpoolmobile.data.DefaultAppContainer
import com.shinyst.coalpoolmobile.data.database.AppRoomDatabase

class CoalPoolMobileApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        val appDb = AppRoomDatabase.getInstance(this)
        container = DefaultAppContainer(this, appDb)
    }
}