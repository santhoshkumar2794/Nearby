package com.santhosh.nearby.ui.main.utils

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.santhosh.nearby.RoutesItem
import com.santhosh.nearby.view.MainFragment


class Util {
    companion object {

        fun isLocationPermissionEnabled(activity : AppCompatActivity): Boolean {
            return if (ContextCompat.checkSelfPermission(activity.applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MainFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                false
            }
        }

        fun getDirectionPolylines(routes: List<RoutesItem>): List<LatLng> {
            val directionList = ArrayList<LatLng>()
            for (route in routes) {
                val legs = route.legs
                for (leg in legs!!) {
                    val steps = leg.steps!!
                    for (step in steps) {
                        val polyline = step.polyline
                        val points = polyline.points
                        val singlePolyline = decodePoly(points)
                        for (direction in singlePolyline) {
                            directionList.add(direction)
                        }
                    }
                }
            }
            return directionList
        }

        private fun decodePoly(encoded: String): ArrayList<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val p = LatLng(lat.toDouble() / 1E5,
                        lng.toDouble() / 1E5)
                poly.add(p)
            }
            return poly
        }
    }
}