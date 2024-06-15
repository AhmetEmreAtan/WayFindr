package com.example.wayfindr.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.places.PlaceModel

class FilteredAdapter(
    private var places: List<PlaceModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<FilteredAdapter.PlaceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(placeId: String)
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val placeImageView: ImageView = itemView.findViewById(R.id.placeImageView)
        val placeNameTextView: TextView = itemView.findViewById(R.id.placeNameTextView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(places[position].placeId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_places_search, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val currentPlace = places[position]
        holder.placeNameTextView.text = currentPlace.placeName
        Glide.with(holder.itemView.context)
            .load(currentPlace.placeImage)
            .into(holder.placeImageView)
    }

    override fun getItemCount() = places.size

    fun setPlacesList(newPlaces: List<PlaceModel>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    fun getPlaceByPlaceId(placeId: String): PlaceModel? {
        return places.find { it.placeId == placeId }
    }
}

