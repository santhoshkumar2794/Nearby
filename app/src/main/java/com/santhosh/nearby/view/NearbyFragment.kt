package com.santhosh.nearby.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import com.santhosh.nearby.R
import com.santhosh.nearby.ui.main.utils.Util
import com.santhosh.nearby.ui.main.utils.Nearby
import com.santhosh.nearby.ui.main.model.PlacesViewModel
import com.santhosh.nearby.ui.main.model.database.PlaceData
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import java.io.IOException

class NearbyFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: PlacesViewModel
    private var nearByPlace: Nearby.NearByPlace? = null
    private var nearbyLocation: NearbyLocation = NearbyLocation()
    private lateinit var googleMap: GoogleMap
    private lateinit var locationProviderClient: FusedLocationProviderClient

    companion object {
        fun newInstance() = NearbyFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.nearby_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportMapFragment = SupportMapFragment.newInstance()
        supportMapFragment.getMapAsync(this)
        activity!!.supportFragmentManager.beginTransaction().add(R.id.nearby_root, supportMapFragment).commit()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getBlogList().observe(this, Observer {
            val arrayList = ArrayList<PointF>()
            it.mapTo(arrayList) { PointF(it.latitude.toFloat(), it.longitude.toFloat()) }

            nearByPlace = Nearby.getNearby(pointArray = arrayList)
            nearbyLocation.minDist = nearByPlace!!.minDist

            for (placeData in it) {
                if (nearbyLocation.placeA != null && nearbyLocation.placeB != null) {
                    break
                }
                if (nearByPlace!!.pointA == PointF(placeData.latitude.toFloat(), placeData.longitude.toFloat())) {
                    nearbyLocation.placeA = placeData
                }

                if (nearByPlace!!.pointB == PointF(placeData.latitude.toFloat(), placeData.longitude.toFloat())) {
                    nearbyLocation.placeB = placeData
                }
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }

        getDeviceLocation()

        val startPoint = nearbyLocation.placeA
        val endPoint = nearbyLocation.placeB
        if (startPoint != null && endPoint != null) {

            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=place_id:${startPoint!!.id}&destination=place_id:${endPoint!!.id}&key=AIzaSyAf0u_wOwyhRpDBXSkiLv85qusXVCgWG3I"
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

        val startPoint = nearbyLocation.placeA!!
        val endPoint = nearbyLocation.placeB!!

        val startMarker = MarkerOptions().position(LatLng(startPoint.latitude, startPoint.longitude)).title(startPoint.name)
        val endMarker = MarkerOptions().position(LatLng(endPoint.latitude, endPoint.longitude)).title(endPoint.name)

        googleMap.addMarker(startMarker)
        googleMap.addMarker(endMarker)

        val build = CameraPosition.builder().target(LatLng(startPoint.latitude, startPoint.longitude)).zoom(15f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(build))
    }

    private fun getDeviceLocation() {
        if (Util.isLocationPermissionEnabled(activity = (activity as AppCompatActivity?)!!)) {
            val lastLocation = locationProviderClient.lastLocation
            lastLocation.addOnCompleteListener {
                if (it.isSuccessful) {
                    val location = it.result
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MainFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                getDeviceLocation()
            }
        }
    }


    data class NearbyLocation(var placeA: PlaceData? = null, var placeB: PlaceData? = null, var minDist: Double = 0.toDouble())
}