package com.example.wayfindr.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R

class PlacesAdapter(
    private val placeName: ArrayList<String>,
    private val placeDescription: ArrayList<String>,
    private val placeImage: ArrayList<Int>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_places, parent, false)
        return PlacesViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        holder.bind(placeImage[position], placeName[position], placeDescription[position])
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position) // Öğeye tıklama işlemini ilettik
        }
    }

    override fun getItemCount(): Int {
        return placeName.size // Listedeki öğe sayısını döndürün
    }

    inner class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val placesImage: ImageView = itemView.findViewById(R.id.placesImage)
        private val placesName: TextView = itemView.findViewById(R.id.placesName)
        private val placesDescription: TextView = itemView.findViewById(R.id.placesDescription)

        fun bind(imageResId: Int, name: String, description: String) {
            placesImage.setImageResource(imageResId)
            placesName.text = name
            placesDescription.text = description
        }
    }
}
