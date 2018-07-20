package com.santhosh.nearby.ui.main.model.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Places")
    fun getAllPlaces(): DataSource.Factory<Int, PlaceData>

    @Insert
    fun insert(placeData: PlaceData)

    @Delete
    fun delete(placeData: PlaceData)
}