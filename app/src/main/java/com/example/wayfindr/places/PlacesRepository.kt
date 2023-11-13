package com.example.wayfindr.places

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlacesRepository {
    fun isPlaceFavorite(userId: String, placeId: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

        userFavoritesReference.child(placeId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    fun addToFavorites(userId: String, place: PlaceModel) {
        val database = FirebaseDatabase.getInstance()
        val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

        val favoritePlace = mapOf(
            "placeId" to place.placeId,
            "placeName" to place.placeName,
            "placeImage" to place.placeImage,
            "placeDescription" to place.placeDescription
        )

        userFavoritesReference.child(place.placeId).setValue(favoritePlace)
    }

    fun removeFromFavorites(userId: String, placeId: String) {
        val database = FirebaseDatabase.getInstance()
        val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

        userFavoritesReference.child(placeId).removeValue()
    }
}