package com.example.wayfindr.places

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlin.math.pow

class LocationCalculator {

    private val firestore = FirebaseFirestore.getInstance()
    private val placesCollection = firestore.collection("places")

    fun getAllPlaceLocations(): Task<List<GeoPoint>?> {
        val placeLocations = mutableListOf<GeoPoint>()
        return placesCollection
            .get()
            .continueWith { task ->
                if (task.isSuccessful) {
                    for (document in task.result?.documents ?: emptyList()) {
                        val placeLocation = document.getGeoPoint("placeLocation")
                        if (placeLocation != null) {
                            placeLocations.add(placeLocation)
                        }
                    }
                }
                return@continueWith placeLocations
            }
    }

    fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val earthRadius = 6371 // Dünya'nın yarıçapı (km)
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dlon = lon2 - lon1
        val dlat = lat2 - lat1

        val a = Math.sin(dlat / 2).pow(2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}