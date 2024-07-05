package com.example.wayfindr.favorites

import com.example.wayfindr.places.PlaceModel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.places.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable

class FavoritesAdapter(
    private var favoritesList: List<PlaceModel>,
    private val itemClickListener: ItemClickListener,
    private val firebaseAuth: FirebaseAuth,
    private val favoriteRepository: FavoriteRepository
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>(), Serializable {

    inner class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val favoriteImage: ImageView = itemView.findViewById(R.id.placesImage)
        private val favoriteName: TextView = itemView.findViewById(R.id.placesName)
        private val favoriteDescription: TextView = itemView.findViewById(R.id.placesDescription)
        val favoriteAddButton: ImageView = itemView.findViewById(R.id.favoriteAdd)

        fun bind(place: PlaceModel) {
            Glide.with(itemView)
                .load(place.placeImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(favoriteImage)

            favoriteName.text = place.placeName
            favoriteDescription.text = place.placeDescription
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_places, parent, false)

        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val favoritePlace = favoritesList[position]
        holder.bind(favoritePlace)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(favoritePlace.placeId)
        }
        holder.favoriteAddButton.setOnClickListener {
            handleFavoriteButtonClick(holder,favoritePlace)
        }
        updateFavoriteButton(holder,favoritePlace)
    }

    private fun handleFavoriteButtonClick(holder: FavoritesViewHolder, place: PlaceModel) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // isFavoritePlace fonksiyonunu doğrudan burada bir geri çağrı ile kullan
            favoriteRepository.isPlaceFavorite(userId, place.placeId) { isFavorite ->
                if (isFavorite) {
                    favoriteRepository.removeFromFavorites(userId, place.placeId)
                } else {
                    favoriteRepository.addToFavorites(userId, place)
                }

                updateFavoriteButton(holder, place)
            }
        } else {
            Log.d("AuthState", "Geçerli kullanıcı null")
            Toast.makeText(holder.itemView.context, "Lütfen giriş yapınız!", Toast.LENGTH_SHORT).show()

        }
    }

    fun updateFavoriteButton(holder: FavoritesViewHolder, place: PlaceModel) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

            // Her bir mekanın favori olup olmadığını kontrol et
            userFavoritesReference.child(place.placeId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isFavorite = snapshot.exists()

                    // Favori butonunun görüntüsünü güncelle
                    if (isFavorite) {
                        place.isFavorite = true
                        holder.favoriteAddButton.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        place.isFavorite = false
                        holder.favoriteAddButton.setImageResource(R.drawable.ic_heart_outlined)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabase", "Veritabanına erişim hatası: $error")
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    fun setFavoritesList(newFavoritesList: List<PlaceModel>) {
        favoritesList = newFavoritesList
        notifyDataSetChanged()
    }

    fun getPlaceByPlaceId(placeId: String): PlaceModel? {
        return favoritesList.find { it.placeId == placeId }
    }
}
