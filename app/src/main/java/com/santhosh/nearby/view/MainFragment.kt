package com.santhosh.nearby.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azoft.carousellayoutmanager.CarouselLayoutManager
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.azoft.carousellayoutmanager.CenterScrollListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.santhosh.nearby.R
import com.santhosh.nearby.ui.main.model.PlacesViewModel
import com.santhosh.nearby.ui.main.model.database.PlaceData
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment(), OnMapReadyCallback, AdapterCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private var placeAdapter: PlaceAdapter = PlaceAdapter(this)

    companion object {
        const val TOTAL_PLACE_COUNT = 10
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val PLACE_SEARCH_RESULT = 2
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: PlacesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportMapFragment = SupportMapFragment.newInstance()
        supportMapFragment.getMapAsync(this)
        activity!!.supportFragmentManager.beginTransaction().add(R.id.main, supportMapFragment).commit()

        places_search.bringToFront()
        places_search.setOnClickListener {
            val intentBuilder = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
            val intent = intentBuilder.build(activity!!)
            startActivityForResult(intent, PLACE_SEARCH_RESULT)
        }

        view.post {
            recycler_view.adapter = placeAdapter
            recycler_view.bringToFront()

            val layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false)
            } else {
                CarouselLayoutManager(CarouselLayoutManager.VERTICAL, false)
            }
            layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())
            recycler_view.layoutManager = layoutManager
            recycler_view.addOnScrollListener(CenterScrollListener())
            recycler_view.setHasFixedSize(true)
        }

        show_nearby.setOnClickListener {
            if (placeAdapter.itemCount < 2) {
                Toast.makeText(context!!, getString(R.string.nearby_count_check_message), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val nearbyFragment = NearbyFragment.newInstance()
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).addToBackStack(null).commit()
            activity!!.supportFragmentManager.executePendingTransactions()
        }

        show_route.setOnClickListener {
            if (placeAdapter.itemCount < 2) {
                Toast.makeText(context!!, getString(R.string.route_count_check_message), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val routeFragment = RouteFragment.newInstance()
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, routeFragment).addToBackStack(null).commit()
            activity!!.supportFragmentManager.executePendingTransactions()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getBlogList().observe(this, Observer {
            placeAdapter.submitList(it)
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }
        getDeviceLocation()
    }

    private fun isLocationPermissionEnabled(): Boolean {
        return if (ContextCompat.checkSelfPermission(context!!.applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            false
        }
    }

    private fun getDeviceLocation() {
        if (isLocationPermissionEnabled()) {
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
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                getDeviceLocation()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PLACE_SEARCH_RESULT -> {
                if (resultCode == RESULT_OK) {
                    if (placeAdapter.itemCount > TOTAL_PLACE_COUNT) {
                        Toast.makeText(context!!, getString(R.string.delete_places), Toast.LENGTH_LONG).show()
                        return
                    }
                    val place = PlaceAutocomplete.getPlace(context!!, data)
                    val location = place.latLng

                    val placeData = PlaceData()
                    placeData.id = place.id
                    placeData.name = place.name.toString()
                    placeData.address = place.address.toString()
                    placeData.phoneNumber = place.phoneNumber.toString()
                    placeData.latitude = location.latitude
                    placeData.longitude = location.longitude
                    viewModel.insertPlace(placeData)
                }
            }
        }
    }

    override fun onDelete(placeData: PlaceData) {
        viewModel.onDelete(placeData)
    }
}
