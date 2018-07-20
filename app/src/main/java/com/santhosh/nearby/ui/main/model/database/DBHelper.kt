package com.santhosh.nearby.ui.main.model.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.experimental.launch

object DBHelper {
    lateinit var placeDatabase: NearbyDB

    fun initializeDB(context: Context) {
        launch {
            placeDatabase = Room.databaseBuilder(context, NearbyDB::class.java, "NearbyDB").build()
        }.start()
    }
}