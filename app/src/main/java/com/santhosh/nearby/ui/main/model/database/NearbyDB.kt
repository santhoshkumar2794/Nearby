package com.santhosh.nearby.ui.main.model.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [(PlaceData::class)], version = 1)
abstract class NearbyDB : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}