package com.example.wayfindr.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R

class ImageSliderAdapter(
    private val imageList: List<Int>,
    private val captionList: List<String>,
    private val itemClickListener: ItemClickListener // Yeni tıklama dinleyici ekledik
) : RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ImageSliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        holder.bind(imageList[position], captionList[position])
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position) // Öğeye tıklama işlemini ilettik
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ImageSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)

        fun bind(imageResId: Int, caption: String) {
            imageView.setImageResource(imageResId)
            textView.text = caption
        }
    }
}
