package com.example.wayfindr

import com.example.wayfindr.places.PlaceModel
import PlacesAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.favorites.FavoritesAdapter
import com.example.wayfindr.favorites.ItemClickListener
import com.example.wayfindr.places.PlacesDetailFragment
import com.example.wayfindr.places.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Favorites: Fragment() {

    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: PlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val favoriteRepository= FavoriteRepository()

        favoritesRecyclerView = view.findViewById(R.id.recyclerViewFavorite)
        favoritesRecyclerView.layoutManager = LinearLayoutManager(context)
        favoritesAdapter = FavoritesAdapter(emptyList(), itemClickListener, firebaseAuth,favoriteRepository)
        favoritesRecyclerView.adapter = favoritesAdapter

        fetchFavoritePlaces()

        return view
    }

    private val itemClickListener = object : ItemClickListener {
        override fun onItemClick(placeId: String) {
            val selectedPlace = favoritesAdapter.getPlaceByPlaceId(placeId)
            if (selectedPlace != null) {
                showPlaceDetailFragment(selectedPlace)
            }
        }
    }


    private fun showPlaceDetailFragment(selectedPlace: PlaceModel) {
        val fragment = PlacesDetailFragment()
        val bundle = Bundle()
        bundle.putSerializable("selectedPlace", selectedPlace)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun fetchFavoritePlaces() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userFavoritesReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites")

            userFavoritesReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoritePlaces = mutableListOf<PlaceModel>()

                    for (placeSnapshot in snapshot.children) {
                        val favoritePlace = placeSnapshot.getValue(PlaceModel::class.java)
                        favoritePlace?.let {
                            favoritePlaces.add(it)
                        }
                    }

                    favoritesAdapter.setFavoritesList(favoritePlaces)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}