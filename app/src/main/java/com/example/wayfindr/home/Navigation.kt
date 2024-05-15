package com.example.wayfindr.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wayfindr.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng

class Navigation : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var closeBTN: ImageButton

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 333
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        closeBTN = findViewById(R.id.maps_close_btn)
        closeBTN.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, true)

            val location: Location? = provider?.let {
                locationManager.getLastKnownLocation(it)
            }

            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
}
