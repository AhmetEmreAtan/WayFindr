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

class FilteredAdapter(private var placesList: List<PlaceModel>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<FilteredAdapter.PlaceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(placeId: String)
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.placeName)
        val placeImage: ImageView = itemView.findViewById(R.id.placeImage)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(placesList[adapterPosition].placeId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_places_search, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placesList[position]
        holder.placeName.text = place.placeName
        Glide.with(holder.itemView.context)
            .load(place.placeImage)
            .into(holder.placeImage)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    fun setPlacesList(newPlacesList: List<PlaceModel>) {
        placesList = newPlacesList
        notifyDataSetChanged()
    }

    fun getPlaceByPlaceId(placeId: String): PlaceModel? {
        return placesList.find { it.placeId == placeId }
    }
}