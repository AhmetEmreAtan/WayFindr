package com.example.wayfindr.places

data class PlacesDetailsModel(
    val placeName: String,
    val placeAddress: String,
    val placeOpeningHours: String,
    val placePrice: String,
    val placeDetails: String,
    val placeImage: String
) {
    constructor() : this("", "", "", "", "", "")
    constructor(placeName: String, placeAddress: String, placeOpeningHours: String, placePrice: String, placeDetails: String) :
            this(placeName, placeAddress, placeOpeningHours, placePrice, placeDetails, "")
}
