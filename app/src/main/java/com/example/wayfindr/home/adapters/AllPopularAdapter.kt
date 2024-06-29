package com.example.wayfindr.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.home.models.AllPopular

class AllPopularAdapter(private var popularList: List<AllPopular>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AllPopularAdapter.PopularViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(popular: AllPopular)
    }

    inner class PopularViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.placeName)
        val placeLocation: TextView = itemView.findViewById(R.id.placeLocation)
        val placeImage: ImageView = itemView.findViewById(R.id.placeImage)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(popularList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_allpopular, parent, false)
        return PopularViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val popular = popularList[position]
        holder.placeName.text = popular.placeName
        holder.placeLocation.text = popular.placeLocation
        Glide.with(holder.itemView.context)
            .load(popular.placeImage)
            .fitCenter()
            .into(holder.placeImage)
    }

    override fun getItemCount(): Int {
        return popularList.size
    }

    fun setPopularList(newPopularList: List<AllPopular>) {
        popularList = newPopularList
        notifyDataSetChanged()
    }

    fun getPopularById(placeId: String): AllPopular? {
        return popularList.find { it.placeId == placeId }
    }
}
