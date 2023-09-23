package com.example.wayfindr.places

import com.google.firebase.firestore.GeoPoint

data class PlaceModel(
    val placeName: String,
    val placeDescription: String,
    val placeImage: String
)

