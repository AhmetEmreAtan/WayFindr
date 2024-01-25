package com.example.wayfindr.home

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
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlacesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryAdapter(private val placeList: List<CategoryDataModel>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeNameTextView: TextView = itemView.findViewById(R.id.placesName)
        val placeDescriptionTextView: TextView = itemView.findViewById(R.id.placesDescription)
        val placeImageView: ImageView = itemView.findViewById(R.id.placesImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_places, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]

        holder.placeNameTextView.text = place.placeName
        holder.placeDescriptionTextView.text = place.placeDescription

        if (place.placeImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(place.placeImage)
                .into(holder.placeImageView)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}


