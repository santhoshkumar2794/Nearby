package com.santhosh.nearby.ui.main.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Places")
class PlaceData {
    @PrimaryKey
    var id: String = ""

    @ColumnInfo
    var name: String = ""

    @ColumnInfo
    var address: String = ""

    @ColumnInfo
    var phoneNumber: String = ""

    @ColumnInfo
    var latitude: Double = 0.toDouble()

    @ColumnInfo
    var longitude: Double = 0.toDouble()
}