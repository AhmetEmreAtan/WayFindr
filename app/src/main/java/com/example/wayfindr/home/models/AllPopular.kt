package com.example.wayfindr.home.models

import java.io.Serializable

data class AllPopular(
    var placeId: String = "",
    val placeName: String = "",
    val placeDescription: String = "",
    val placeImage: String = "",
    val placeCategories: String? = "",
    val placeAddress: String = "",
    val placeLocation: String = "",
    val placeOpeningHours: String = "",
    val placeDetails: String = "",
    val placePrice: String? = "",
    var isFavorite: Boolean = false
):Serializable

