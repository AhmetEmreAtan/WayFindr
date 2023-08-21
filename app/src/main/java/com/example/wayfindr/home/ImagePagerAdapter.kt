package com.example.wayfindr.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R

class ImagePagerAdapter(private val context: Context) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    private var imageList = listOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_layout, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageResId = imageList[position]
        holder.bind(imageResId)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun setImageList(images: List<Int>) {
        imageList = images
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(imageResId: Int) {
            // ImageView veya başka bir görüntüleme bileşeni ile görseli görüntüleyin
            // Örneğin:
            // val imageView = itemView.findViewById<ImageView>(R.id.imageView)
            // imageView.setImageResource(imageResId)
        }
    }
}

