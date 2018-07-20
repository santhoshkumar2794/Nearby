package com.santhosh.nearby.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.santhosh.nearby.Directions
import com.santhosh.nearby.MainActivity
import com.santhosh.nearby.R
import com.santhosh.nearby.ui.main.model.PlacesViewModel
import com.santhosh.nearby.ui.main.model.database.PlaceData
import com.santhosh.nearby.ui.main.utils.Util
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import java.io.IOException

class RouteFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: PlacesViewModel
    private lateinit var googleMap: GoogleMap
    var placeList: PagedList<PlaceData>? = null

    companion object {
        fun newInstance() = RouteFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.route_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportMapFragment = SupportMapFragment.newInstance()
        supportMapFragment.getMapAsync(this)
        activity!!.supportFragmentManager.beginTransaction().add(R.id.route_root, supportMapFragment).commit()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getBlogList().observe(this, Observer {
            placeList = it
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }

        if (placeList != null) {
            val startPoint = placeList!![0]
            val endPoint = placeList!![placeList!!.size - 1]
            var origin : StringBuilder = StringBuilder("origin=")
            var destination : StringBuilder = StringBuilder("destination=")
            var waypoint : StringBuilder = StringBuilder("waypoints=")
            for (i in 0 until placeList!!.size) {
                val placeData = placeList!![i]!!
                when (i) {
                    0 -> origin.append("place_id:${placeData.id}")
                    placeList!!.size-1 -> {
                        destination.append("place_id:${placeData.id}")
                    }
                    else -> {
                        waypoint.append("place_id:${placeData.id}")
                    }
                }
            }
            val url = "https://maps.googleapis.com/maps/api/directions/json?$origin&$destination&$waypoint&key=${MainActivity.API_KEY}"
            val request = Request.Builder().url(url).build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(request: Request?, e: IOException?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(response: Response?) {
                    val directions = Gson().fromJson<Directions>(response!!.body().string(), Directions::class.java)
                    Handler(Looper.getMainLooper()).post {
                        drawRoute(directions)
                    }
                }
            })
        }
    }

    private fun drawRoute(directions: Directions) {
        val polylines = Util.getDirectionPolylines(directions.routes!!)

        val polylineOptions = PolylineOptions()
        polylineOptions.width(15f)
        polylineOptions.color(Color.BLUE)
        polylineOptions.addAll(polylines)

        googleMap.addPolyline(polylineOptions)

        for (i in 0 until placeList!!.size) {
            val placeData = placeList!![i]!!

            val marker = MarkerOptions().position(LatLng(placeData.latitude, placeData.longitude)).title(placeData.name)
            googleMap.addMarker(marker)
        }

        val startPoint = placeList!![0]!!
        val build = CameraPosition.builder().target(LatLng(startPoint.latitude, startPoint.longitude)).zoom(15f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(build))
    }

}