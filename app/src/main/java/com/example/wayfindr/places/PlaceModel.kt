package com.example.wayfindr.places

import android.os.Parcel
import android.os.Parcelable

data class PlaceModel(
    val placeName: String,
    val placeDescription: String,
    val placeImage: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(placeName)
        parcel.writeString(placeDescription)
        parcel.writeString(placeImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaceModel> {
        override fun createFromParcel(parcel: Parcel): PlaceModel {
            return PlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<PlaceModel?> {
            return arrayOfNulls(size)
        }
    }
}
