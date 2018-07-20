package com.zestworks.blogger.model.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.santhosh.nearby.ui.main.model.database.DBHelper
import com.santhosh.nearby.ui.main.model.database.PlaceData
import kotlinx.coroutines.experimental.launch

class PlaceRepository : Repository {

    override fun getPlaceList(): LiveData<PagedList<PlaceData>> {
        val places = DBHelper.placeDatabase.placeDao().getAllPlaces()
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(true)
                .build()
        return LivePagedListBuilder(places, config).build()
    }

    override fun insertPlace(placeData: PlaceData) {
        launch {
            DBHelper.placeDatabase.placeDao().insert(placeData)
        }.start()
    }

    override fun deletePlace(placeData: PlaceData) {
        launch {
            DBHelper.placeDatabase.placeDao().delete(placeData)
        }.start()
    }

}