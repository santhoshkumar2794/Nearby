package com.santhosh.nearby.view

import com.santhosh.nearby.ui.main.model.database.PlaceData

interface AdapterCallback {
    fun onDelete(placeData: PlaceData)
}