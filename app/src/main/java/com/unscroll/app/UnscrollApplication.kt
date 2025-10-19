package com.unscroll.app

import android.app.Application
import com.unscroll.shared.di.SharedContainer

class UnscrollApplication : Application() {
    lateinit var container: SharedContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = SharedContainer(applicationContext)
    }
}
