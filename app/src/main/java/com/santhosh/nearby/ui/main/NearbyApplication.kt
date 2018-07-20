package com.santhosh.nearby.ui.main

import android.app.Application
import com.santhosh.nearby.ui.main.model.database.DBHelper

class NearbyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DBHelper.initializeDB(this)
    }
}