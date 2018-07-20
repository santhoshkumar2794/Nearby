package com.zestworks.blogger.model.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.santhosh.nearby.ui.main.model.database.PlaceData

interface Repository {

    fun getPlaceList() : LiveData<PagedList<PlaceData>>

    fun insertPlace(placeData: PlaceData)
    fun deletePlace(placeData: PlaceData)
}