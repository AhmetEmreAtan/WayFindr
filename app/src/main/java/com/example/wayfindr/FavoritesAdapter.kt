package com.example.wayfindr

import PlaceModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.places.ItemClickListener
import java.io.Serializable

class FavoritesAdapter(
    private var favoritesList: List<PlaceModel>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>(), Serializable {

    inner class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val favoriteImage: ImageView = itemView.findViewById(R.id.placesImage)
        private val favoriteName: TextView = itemView.findViewById(R.id.placesName)
        private val favoriteDescription: TextView = itemView.findViewById(R.id.placesDescription)

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
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    fun setFavoritesList(newFavoritesList: List<PlaceModel>) {
        favoritesList = newFavoritesList
        notifyDataSetChanged()
    }
}
