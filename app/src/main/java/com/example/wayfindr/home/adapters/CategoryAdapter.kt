package com.example.wayfindr.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.home.models.CategoryDataModel

class CategoryAdapter(
    private val placeList: List<CategoryDataModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(place: CategoryDataModel)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeNameTextView: TextView = itemView.findViewById(R.id.placesName)
        val placeDescriptionTextView: TextView = itemView.findViewById(R.id.placesDescription)
        val placeImageView: ImageView = itemView.findViewById(R.id.placesImage)

        fun bind(place: CategoryDataModel, clickListener: OnItemClickListener) {
            placeNameTextView.text = place.placeName
            placeDescriptionTextView.text = place.placeDescription

            if (place.placeImage.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(place.placeImage)
                    .into(placeImageView)
            }

            itemView.setOnClickListener {
                clickListener.onItemClick(place)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_places, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.bind(place, itemClickListener)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}