package com.example.wayfindr.places

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PlacesAdapter (val placeName:ArrayList<String>,val placeDescription:ArrayList<String>,val placeImage:ArrayList<Int>):RecyclerView.Adapter<PlacesAdapter.Places>(){

    class Places(itemView: View):RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Places {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: Places, position: Int) {
        TODO("Not yet implemented")
    }


}