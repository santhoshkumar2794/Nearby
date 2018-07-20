package com.santhosh.nearby.ui.main.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.santhosh.nearby.ui.main.model.database.PlaceData
import com.zestworks.blogger.model.repository.PlaceRepository
import com.zestworks.blogger.model.repository.Repository

class PlacesViewModel : ViewModel() {
    private var placeRepository: Repository = PlaceRepository()

    fun getBlogList(): LiveData<PagedList<PlaceData>> {
        return placeRepository.getPlaceList()
    }

    fun insertPlace(placeData: PlaceData){
        placeRepository.insertPlace(placeData)
    }

    fun onDelete(placeData: PlaceData) {
        placeRepository.deletePlace(placeData)
    }
}
