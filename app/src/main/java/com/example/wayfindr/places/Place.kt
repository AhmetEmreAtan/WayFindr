package com.example.wayfindr.places

import com.google.firebase.firestore.GeoPoint

data class Place(
    val placeName: String,
    val placeDescription: String,
    val placeImage: String,
    val placePrice: Boolean, //Ücretsizse true, ücretliyse false
    val placeCategories: List<String>,
    val placeLocation: GeoPoint? = null
)
